---
title: "Datomic Pro Change Log"
site: "Datomic"
source: "https://docs.datomic.com/changes/pro.html"
description: "Explore the latest updates and changes in Datomic Pro. Stay informed about new features, improvements, and bug fixes."
word_count: 9654
---

## Read This First

- This document covers all releases. For a summary of critical release notices only, see [Release Notices](https://docs.datomic.com/release-notices.html).
- Releases with four version number components are bugfix **only**: releases based on their three-component parents, e.g. 0.8.4020.24 is a bugfix release for 0.8.4020. When upgrading, you should always choose the highest-numbered bugfix release in a family of releases.
- The Datomic team recommends that you always test new releases in a staging environment first.
- The Datomic team recommends that you always take a backup before adopting a new release.

## 1.0.7705: 2026/07/10

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7705.zip` | 2026/07/10 | 1.0.7705 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7705/datomic-pro-1.0.7705.zip) |
| `com.datomic/peer` | 2026/07/10 | 1.0.7705 | `Peer dep (leiningen)`   `Peer dep (deps.edn)` |

- Feature: Permanently discontinue maintenance of a composite tuple attribute. Run upgrade-schema to get access. See [Discontinuing Composite Tuples](https://docs.datomic.com/schema/schema-reference.html#discontinuing-composite-tuples) for more details.
- Upgrade: Datomic now requires Java 17 or greater.
- Upgrade: Datomic now supports Java 25. Two benign warnings may appear: a sun.misc.Unsafe deprecation on Peers and Transactors (suppress with –sun-misc-unsafe-memory-access=allow) and a restricted java.lang.System method on Peers (suppress with –enable-native-access=ALL-UNNAMED).
- Upgrade: Datomic now supports ZGC on Java 25.
- Upgrade: Datomic now requires SLF4J 2.0.17. Minimum versions for other logging deps have also been updated, please see [Configuring Logging](https://docs.datomic.com/operation/configuring-logging.html) for specifics.
- Performance: improved index read performance.
- Fix: prevent blanks `(_)` from unifying across function clauses in query.
- Fix: SQL protocol connection map:factory parameter now works properly.

## 1.0.91: 2026/07/10 - Client-Pro

- Feature: [seek-datoms](https://docs.datomic.com/indexes/rseek-datoms.html) - index access to raw datoms.
- Feature: [rseek-datoms](https://docs.datomic.com/indexes/rseek-datoms.html) - reverse complement of seek-datoms.
- Fix: datomic.client.api/with-db values now correctly handle temporal filters (as-of/since/history).

## 1.0.7622: 2026/04/28

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7622.zip` | 2026/04/28 | 1.0.7622 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7622/datomic-pro-1.0.7622.zip) |
| `[com.datomic/peer "1.0.7622"]` | 2026/04/28 | 1.0.7622 | `Copy Peer dep` |

- Feature: Read-only connections to storage and backups  
	See [Read Only Connections](https://docs.datomic.com/operation/read-only.html).
- Feature: rseek-datoms - reverse index iteration, complement to seek-datoms  
	See [rseek-datoms API doc](https://docs.datomic.com/clojure/index.html#datomic.api/rseek-datoms).
- Performance: Reduce log write amplification for systems with many transactions
- New API: list-backups - lists the timepoints available in a backup repository  
	See [list-backups API doc](https://docs.datomic.com/clojure/index.html#datomic.api/list-backups).
- Performance: Reduce CPU and memory required to calculate index metrics
- Fix: Regression introduced in 1.0.7556 which broke the ddb-local protocol

## 1.0.7556: 2026/03/13

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7556.zip` | 2026/03/13 | 1.0.7556 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7556/datomic-pro-1.0.7556.zip) |
| `[com.datomic/peer "1.0.7556"]` | 2026/03/13 | 1.0.7556 | `Copy Peer dep` |

- Fix: Prevent issue when running both io-stats and query-stats where `:ret` would nest.
- Performance: Significantly reduce CPU and memory allocation of Datomic's memory-size routine.
- Dependency upgrade: Datomic now uses AWS Java SDK v2, and has removed dependencies on v1

Peers connecting to DynamoDB storage must depend on `software.amazon.awssdk/dynamodb {:mvn/version "2.31.45"}`

Apps that use AWS SDK v1 classes `com.amazonaws.*` implicitly should make explicit their dependency on v1 libraries or migrate usages to v2.

If your application uses certain logging backends, AWS Java SDK v2 requires these minimum versions

- logback-classic -\> 1.4.14
- org.slf4j/slf4j-jdk14 -\> 1.7.36
- org.slf4j/slf4j-log4j12 -\> 1.7.36

## 1.0.7491: 2026/01/26

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7491.zip` | 2026/01/26 | 1.0.7491 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7491/datomic-pro-1.0.7491.zip) |
| `[com.datomic/peer "1.0.7491"]` | 2026/01/26 | 1.0.7491 | `Copy Peer dep` |

- Fix: Ignore component relationships established after an excision request
- Performance: excision-repair-tool reporting and repairing performance is significantly improved.

## 1.0.7482: 2026/01/06

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7482.zip` | 2026/01/06 | 1.0.7482 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7482/datomic-pro-1.0.7482.zip) |
| `[com.datomic/peer "1.0.7482"]` | 2026/01/06 | 1.0.7482 | `Copy Peer dep` |

- Performance: Improve indexing performance, reduce I/O in large databases

## 1.0.7469: 2025/10/23

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7469.zip` | 2025/10/23 | 1.0.7469 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7469/datomic-pro-1.0.7469.zip) |
| `[com.datomic/peer "1.0.7469"]` | 2025/10/23 | 1.0.7469 | `Copy Peer dep` |

- Fix: Indexing slowly leaks garbage segments in storage which are unrecoverable by `gc-storage`.
- Fix: excisions may not be completely applied to [as-of](https://docs.datomic.com/clojure/index.html#datomic.api/as-of) or [history](https://docs.datomic.com/clojure/index.html#datomic.api/history) database values

Datomic Release 1.0.7469 fixes a bug that prevented excisions from removing datoms from the [as-of](https://docs.datomic.com/clojure/index.html#datomic.api/as-of) or [history](https://docs.datomic.com/clojure/index.html#datomic.api/history) indexes. This release also includes a tool to detect incomplete excisions to the index and optionally re-apply them. This bug only affects databases that contain excision datoms (`:db/excise`). This bug does not affect ordinary database values (those without [as-of](https://docs.datomic.com/clojure/index.html#datomic.api/as-of) or [history](https://docs.datomic.com/clojure/index.html#datomic.api/history)), nor the transaction log.

See: [Excision Repair Tool](https://docs.datomic.com/release-notices.html#excision-repair-tool)

The tool works in both report and repair modes while the transactor runs. The tool needs read permissions to storage, like a peer. Repair mode requires write permissions.

## 1.0.7394: 2025/08/07

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7394.zip` | 2025/08/07 | 1.0.7394 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7394/datomic-pro-1.0.7394.zip) |
| `[com.datomic/peer "1.0.7394"]` | 2025/08/07 | 1.0.7394 | `Copy Peer dep` |

- Fix: Prevent a problem where in rare situations indexing jobs can fail to progress indefinitely.

## 1.0.7387: 2025/06/27

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7387.zip` | 2025/06/27 | 1.0.7387 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7387/datomic-pro-1.0.7387.zip) |
| `[com.datomic/peer "1.0.7387"]` | 2025/06/27 | 1.0.7387 | `Copy Peer dep` |

- Performance: Reduce Peer and Transactor memory usage in large databases
- Performance: Improve indexing performance, reduce I/O in large databases
- Upgraded org.clojure/core.async to 1.8.741

## 1.0.7364: 2025/05/08

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7364.zip` | 2025/05/08 | 1.0.7364 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7364/datomic-pro-1.0.7364.zip) |
| `[com.datomic/peer "1.0.7364"]` | 2025/05/08 | 1.0.7364 | `Copy Peer dep` |

- Datomic peer library now requires Clojure 1.11.4 or greater. See [Release Notice \[Datomic Pro Peer\]: Updating to Clojure 1.11.4](https://forum.datomic.com/t/release-notice-datomic-pro-peer-updating-to-clojure-1-11-4/2503).
- Fix: Calling d/transact or d/transact-async with nil tx-data no longer terminates the transactor.
- Fix: memory protocol databases require an attribute be indexed when adding uniqueness constraint.
- Enhancement: Default to index-parallelism when adding an AVET index.
- Enhancement: Reduced size of Datomic zip distribution.

## 1.0.7277: 2024/12/16

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7277.zip` | 2024/12/16 | 1.0.7277 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7277/datomic-pro-1.0.7277.zip) |
| `[com.datomic/peer "1.0.7277"]` | 2024/12/16 | 1.0.7277 | `Copy Peer dep` |

- Performance: Reduce memory required for indexing.

## 1.0.7260: 2024/10/22

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7260.zip` | 2024/10/22 | 1.0.7260 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7260/datomic-pro-1.0.7260.zip) |
| `[com.datomic/peer "1.0.7260"]` | 2024/10/22 | 1.0.7260 | `Copy Peer dep` |

- Performance: Reduce memory required to calculate db-stats.
- Performance: Reduce memory required to calculate index-metrics.
- Feature: `datomic.api/with` now supports io-stats.
- Feature: hints. See [Reducing Latency with Transaction Hints](https://docs.datomic.com/reference/hints.html).

## 1.0.7187: 2024/08/22

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7187.zip` | 2024/08/22 | 1.0.7187 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7187/datomic-pro-1.0.7187.zip) |
| `[com.datomic/peer "1.0.7187"]` | 2024/08/22 | 1.0.7187 | `Copy Peer dep` |

- Upgraded Clojure to 1.11.4 in the transactor. See [Clojure 1.11.4 release notice](https://clojure.org/news/2024/08/03/clojure-1-11-4).
- Performance: Reduce transactor memory usage when indexing, particularly when adding an AVET index.
- Performance: Reduce memory required by transactors and peers.

## 1.0.7180: 2024/07/11

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7180.zip` | 2024/07/11 | 1.0.7180 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7180/datomic-pro-1.0.7180.zip) |
| `[com.datomic/peer "1.0.7180"]` | 2024/07/11 | 1.0.7180 | `Copy Peer dep` |

- Feature: tx-stats. See [APIs \> Tx-Stats](https://docs.datomic.com/reference/tx-stats.html).
- Feature: New Cassandra3 protocol with the V4 Datastax driver. See [Operation \> Provisioning Cassandra3](https://docs.datomic.com/operation/storage.html#cassandra).
- Performance: Improve performance in transactions and calls to `datomic.api/with` by prefetching reads. Config option datomic.prefetchConcurrency limits the number of concurrent prefetches. See [Operation \> System Properties](https://docs.datomic.com/operation/system-properties.html).
- Performance: Improved transaction performance.
- Fix: Regression introduced in 1.0.7010 where the peer jar includes compiled Clojure libraries. If your app has an existing dependency on core.async, ensure that you depend on 1.6.681 or later. An older version will cause the peer to throw a ClassCastException when connecting to a database. See the [core.async changelog](https://github.com/clojure/core.async?tab=readme-ov-file#changelog) for more details.
- Fix: Regression introduced in 1.0.7010 where continuous heavy transaction load could cause unbounded memory use on the transactor.
- Fix: TransactionApplyMsec metric now includes transactions which took less than a millisecond to apply.
- Fix: Ensure datoms are virtual and are no longer added to the log.
- Upgraded Clojure to 1.11.3 in the transactor.
- Upgraded com.datomic/client-pro to 1.0.81
- Upgraded bootstrap.js in the REST API server to 5.3.3 addresses CVE-2016-10735.
- Datomic now supports Java 21.

## 1.0.81: 2024/07/11 - Client-Pro

- Upgraded com.cognitect/transit-clj to 1.0.333
- Upgraded com.cognitect/http-client to 1.0.127

## 1.0.78: 2024/02/12 - Client-Pro

Fix: correctly deserialize URIs to java.net.URI

## 1.0.7075: 2023/12/18

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7075.zip` | 2023/12/18 | 1.0.7075 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7075/datomic-pro-1.0.7075.zip) |
| `[com.datomic/peer "1.0.7075"]` | 2023/12/18 | 1.0.7075 | `Copy Peer dep` |

- Peer introduces a new dependency on Apache commons-io. If your app has an existing dependency on commons-io, ensure that you depend on 2.15.1 or greater. An older version will cause the peer to throw either a ClassNotFoundExceptionArray when initializing or an IndexOutOfBoundsException when connecting to a database.
- New GcPauseMsec metric. Check [Transactor Metrics](https://docs.datomic.com/operation/monitoring.html#transactor-metrics).
- Fix: pull expression could return true when supplying a:default for a boolean attribute.
- Fix: regression introduced in 1.0.6527 that could cause a database with blank keyword idents to fail to load.
- Fix: Datomic could incorrectly calculate the delay for a StorageBackoff, leading to a longer than expected delay before retry.
- Performance: replace BufferedInputStream with an unsynchronized stream, important for performance on Java 17 and later.
- Enhancement: provide a better error message when d/connect fails to make a connection.
- Enhancement: increase the number of retries when calling verify-backup on s3 storage.
- Upgraded com.amazonaws/aws-java-sdk-bundle to 1.12.564.
- Upgraded com.cognitect/http-client to 1.0.126.
- Upgraded com.cognitect/transit-clj to 1.0.333.
- Upgraded com.datomic/client-pro to 1.0.77.
- Upgraded com.datomic/memcache-asg-java-client to 1.1.0.36.
- Upgraded com.google.guava/guava to 32.0.1-jre.
- Upgraded org.apache.httpcomponents/httpclient to 4.5.13.
- Upgraded org.apache.httpcomponents/httpcore to 4.4.13.
- Upgraded org.clojure/core.async to 1.6.681.
- Upgraded org.clojure/core.match to 1.0.1.
- Upgraded org.clojure/math.combinatorics to 0.2.0.
- Upgraded org.eclipse.jetty/jetty-\* to 9.4.53.v20231009.
- Upgraded org.msgpack/msgpack to 0.6.12.
- Upgraded org.apache.activemq/artemis\* to 2.31.2.

## 1.0.77: 2023/12/18 - Client-Pro

- Upgraded com.cognitect/http-client to 1.0.126.
- Upgraded commons-codec to 1.16.0.
- Upgraded org.clojure/core.async to 1.6.681.
- Upgraded org.eclipse.jetty/jetty-\* to 9.4.52.v20230823.
- Upgraded org.msgpack/msgpack to 0.6.12.

## 1.0.7021: 2023/10/19

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7021.zip` | 2023/10/19 | 1.0.7021 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.7021/datomic-pro-1.0.7021.zip) |
| `[com.datomic/peer "1.0.7021"]` | 2023/10/19 | 1.0.7021 | `Copy Peer dep` |

[Critical Fixes](https://docs.datomic.com/release-notices.html#1.0.7021):

- Fixed regression introduced in 1.0.7010 where query thread pool could deadlock under highly concurrent load, preventing queries from completing.
- Fixed a regression introduced in transactors in 1.0.7010: Peer tx-range can temporarily fail to return a durably-logged transaction coincident with the start of an indexing job until a few more transactions are processed.

## 1.0.7010: 2023/10/10

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.7010.zip` | 2023/10/10 | 1.0.7010 | Download Removed |
| `[com.datomic/peer "1.0.7010"]` | 2023/10/10 | 1.0.7010 |  |

- Performance: improved storage and cache stack performance.
- Performance: log tree production is now async and separate from transactions.
- Performance: reduced resources required by indexing.
- Performance: improved S3 backup and restore performance.
- New CloudWatch metric: `TransactionApplyMsec`. Check [Transactor Metrics](https://docs.datomic.com/operation/monitoring.html#transactor-metrics).
- New System Property: `ddbRequestTimeout`. Check [System Properties](https://docs.datomic.com/operation/system-properties.html).

## 1.0.6735: 2023/06/30

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.6735.zip` | 2023/06/30 | 1.0.6735 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.6735/datomic-pro-1.0.6735.zip) |
| `[com.datomic/peer "1.0.6735"]` | 2023/06/30 | 1.0.6735 | `Copy Peer dep` |

- Feature: [separated Garbage Collection](https://docs.datomic.com/operation/capacity.html#separated-garbage-collection) tool for Datomic.
- Fixed regression introduced in 1.0.6725 that disabled health check endpoint.

## 1.0.6733: 2023/05/18

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.6733.zip` | 2023/05/18 | 1.0.6733 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.6733/datomic-pro-1.0.6733.zip) |
| `[com.datomic/peer "1.0.6733"]` | 2023/05/18 | 1.0.6733 | `Copy Peer dep` |

Fixed regressions introduced in 1.0.6711:

- Nested entities can incorrectly cause a transaction to fail with a `db.error/cycle-in-affinity`.
- Component entities of a transaction entity are all assigned the entity id of the transaction.

## 1.0.6726: 2023/04/27

| Artifact | Release Date | Version | Release |
| --- | --- | --- | --- |
| `datomic-pro-1.0.6726.zip` | 2023/04/27 | 1.0.6726 | [Download](https://datomic-pro-downloads.s3.amazonaws.com/1.0.6726/datomic-pro-1.0.6726.zip) |
| `[com.datomic/peer "1.0.6726"]` | 2023/04/27 | 1.0.6726 | `Copy Peer dep` |

- The peer and transactor binaries are now licensed under the Apache 2.0 license.
- You do not need to specify a license key in the transactor properties file.
- The peer library is now available under the artifact name com.datomic/peer in Maven Central.
- Any com.datomic support libraries needed by the peer are available in Maven Central.
- The spymemcached library used by Datomic has been shaded to the package name datomic.spy.memcached to avoid conflict with other forks of spymemcached.

## 1.0.6711: 2023/04/20

- Feature: [implicit partitions](https://docs.datomic.com/schema/schema.html#implicit-partitions).
- Feature: *:db/force-partition* and *:db/match-partition*. Check [Partition assignment](https://docs.datomic.com/transactions/partitions.html#partition-assignment).
- Upgrade: the transactor and peer now require an LTS version of Java 1.11 or greater.
- Upgraded artemis to 2.28.0.
- Upgraded caffeine to 3.1.5.
- Upgraded ring to 1.10.0.

## 1.0.6644: 2023/04/03

- Performance: indexing is more aggressive in removing `:db/noHistory` datoms.
- Fix: transaction no longer times out if a user transaction function puts unfressianable data in an exception.
- Fix: very rare situation where a transaction could be included in the index but not in the log.
- Upgraded H2 to 2.1.214.

## 1.0.76: 2023/02/27 - Client-Pro

- Feature: [Io-stats](https://docs.datomic.com/reference/io-stats.html) for transactions and queries.
- Feature: [Query-stats](https://docs.datomic.com/reference/query-stats.html) for transactions and queries.

## 1.0.6610: 2023/01/25

- Feature: [query-stats](https://docs.datomic.com/reference/query-stats.html).
- Performance: improved performance accessing DDB with high write concurrency.
- Performance: improved performance accessing S3 with high write concurrency.
- Performance: improved fressian read.
- Fix: pull performance regression introduced in 1.0.6527.
- Fix: exception when using io-stats in nested contexts, you should no longer see "No implementation of method::-merge-stats of protocol".
- Fix: [restart memcached client when needed to work around](https://issues.couchbase.com/browse/SPY-196).
- Fix: fail fast if indexing thread is hung waiting on storage response.
- Upgraded PostgreSQL driver to 42.5.1.
- Upgraded AWS libraries to 1.12.358.

## 1.0.6527: 2022/10/21

- Feature: [io-stats](https://docs.datomic.com/reference/io-stats.html) for transactions and queries.
- Feature: [verify backups](https://docs.datomic.com/operation/backup.html#verifying-backups) without performing a restore.
- Performance: improved peer adoption of new indexes.
- Performance: improved performance (throughput and $) for differential backups to S3.
- Performance: Datomic now uses caffeine instead of guava for the object cache.
- Performance improvements for Valcache and the object cache.
- Performance: limit thread and memory usage when the transactor is [adding an AVET index](https://docs.datomic.com/schema/schema-change.html#adding-avet-index).
- Fix: peer no longer resolves lookup refs before sending tx data to transactor.
- Fix: attribute names that begin with an underscore can be used for forward lookups in entity and pull. Note that naming attributes with a leading underscore is strongly discouraged because it conflicts with the convention for reverse lookup.

## 1.0.6397: 2022/04/05

- New API: add:release-object-cache to [d/administer-system](https://docs.datomic.com/clojure/index.html#datomic.api/administer-system).
- New: expose configuration to set the Memcached item expiration in days. Check [System Properties](https://docs.datomic.com/operation/system-properties.html).
- Feature: support for DynamoDB in additional regions (including eu-north-1).
- Feature: add support for JDK17.
- Notes: updated EULA.
- Upgraded artemis to 2.19.1.
- Upgraded com.datomic/memcache-asg-java-client to 1.1.0.33.
- Upgraded transit-clj to 1.0.329.
- Upgraded aws-sdk-bundle to 1.12.132.
- Upgraded client-pro to 1.0.75.
- Upgraded Clojure to 1.11.0 in transactor.

## 1.0.75: 2022/04/05 - Client-Pro

Upgrade Client to 1.0.126.

## 1.0.6362: 2022/01/05

- Fix: don't print or log connection info by default in peer-server.
- Fix: regression introduced in 1.0.6344 preventing the use of `:db-after` with peer-server.
- Fix: replace slf4j-nop dependency with slf4j-api.
- Upgraded com.datomic/memcache-asg-java-client to 1.1.0.32.
- Upgraded embedded REBL to 0.9.242.
- Upgraded core.async to 1.5.648.
- Upgraded commons-codec to 1.15.
- Upgraded Jetty to 9.4.44.v20210927.
- Upgraded logback-classic to 1.2.8.
- Upgraded org.slf4j libs to 1.7.32.
- Upgraded Guava to 31.0.1.
- Upgraded AWS Java SDK to 1.12.100.
- Upgraded Tomcat-JDBC to 7.0.109.

## 1.0.74: 2022/01/05 - Client-Pro Update

- Upgraded AWS Java SDK to 1.12.100.
- Upgraded Jetty to 9.4.44.v20210927.
- Upgraded core.async to 1.5.648.
- Upgraded core.cache to 1.0.225.
- Upgraded data.priority-map to 1.1.0.
- Upgraded http-client to 1.0.110.

## 0.1.233 2022/01/05 - Console Update

- Upgraded Jetty to 9.4.44.v20210927
- Upgraded logback-classic to 1.2.8

## 1.0.6344: 2021/09/15

- New API: `Database.dbStats`. Check [dbStats](https://docs.datomic.com/javadoc/datomic/Database.html#dbStats--).
- Fix: prevent a problem where in rare situations indexing jobs can fail repeatedly with an `ArityException` in the transactor log.
- Fix: prevent a problem where in rare situations indexing jobs can fail to progress, leading to `AlarmBackPressure` and operators needing to restart the transactor process.
- Fix: ensure that `d/log` is only as recent as `d/db`.
- Fix: regression introduced in 1.0.6316 that caused memcache client to fail to start when using SASL authentication.
- Fix: regression introduced in 1.0.6316 that could prevent reporting of metrics to Cloudwatch.
- Fix: redact memcache password in log data.
- Fix: don't print connection URI to log.
- Fix: don't expose database credentials when printing `db` value.
- Change the transactor property `datomic.printConnectionInfo` default to false.
- Enhancement: bin/maven-install now installs memcache-asg-java-client to the local maven repository.
- Upgraded Clojure to 1.10.3.
- Upgraded AWS Java SDK to 1.12.1.
- Upgraded Jetty to 9.4.41.v20210516.
- Upgraded ActiveMQ Artemis to 2.17.0.
- Upgraded core.async to 1.3.618.
- Upgraded data.json to 2.4.0.
- Upgraded tools.cli to 1.0.206.
- Upgraded org.fressian to 0.6.6.
- Upgraded transit-clj to 1.0.324.

## 1.0.72: 2021/09/15 - Client-Pro Update

- Upgraded core.async to 1.3.618.
- Upgraded transit-clj to 1.0.324.
- Upgraded Jetty to 9.4.41.v20210516.

## 0.1.227 2021/09/15 - Console Update

- Upgraded Clojure to 1.10.3.
- Upgraded Jetty to 9.4.41.v20210516.
- Upgraded tools.cli to 1.0.206.

## 1.0.6316: 2021/07/22

- New feature: AWS memcached auto-discovery Check [node auto discovery](https://docs.datomic.com/operation/caching.html#node-auto-discovery).
- Improved performance for query under heavy parallel load.
- Fix: attribute specs respect `false` values when ensuring attribute existence.

## 1.0.6269: 2021/03/09

Changed in 1.0.6269

- Fixed intermittent ClassCastException in `:reverse` direction of `index-pull`.
- Improved performance for query under heavy parallel load.
- Improved performance for indexing after excision.
- Upgraded ActiveMQ Artemis to 2.14.0.
- Upgraded guava to 30.1-jre.
- Upgraded Clojure contrib deps to their 1.0.x versions.
- Upgraded org.slf4j libs to 1.7.30.
- Upgraded AWS Java SDK to 1.11.946.

## 1.0.6242: 2021/01/27

Changed in 1.0.6242

- Fix: [attribute predicates](https://docs.datomic.com/schema/schema.html#attribute-predicates) are now applied to assertions only.
- Upgrade to presto 348 for [Datomic analytics](https://docs.datomic.com/analytics/analytics-cli.html).

## 0.9.66: 2021/01/20 - Client-pro update

Improvment: make client dynaload thread-safe.

## 1.0.6222: 2020/11/23

Changed in 1.0.6222

- New: change the scale of a [BigDecimal attribute](https://docs.datomic.com/schema/schema.html#value-types) in a transaction.
- New: add `:apply-msec` key to `:tx/process` events in the log, useful for identifying slow transactions.
- Upgrade to Presto 346 for [Datomic analytics](https://docs.datomic.com/analytics/analytics-concepts.html).

## 1.0.6202: 2020/08/06

Changed in 1.0.6202

- New feature: [cancel](https://docs.datomic.com/transactions/transaction-functions.html#canceling).
- Upgrade: the version of the bundled Presto server is now 338. Note: This upgrade now requires Java 11 to run the Presto server.
- Fix: prevent a race condition in Valcache that could lead to a failed read.
- Fix: regression introduced in 1.0.6165 that caused Datomic Console to fail to start on recent versions of Datomic.
- Fix: prevent a situation where `tx-range` sometimes returns one extra transaction before a specified instant.

## 0.9.63: 2020/07/21 - Client-Pro Update

Fix: datomic.api.client.async/client now accepts:dev-local as a:server-type.

## 0.9.62: 2020/07/17 - Client-Pro Update

Add support for [dev-local](https://docs.datomic.com/datomic-local.html) clients.

## Console 0.1.225 2020/05/29

Fixed bug that caused Console to fail to start on newer versions of Datomic.

> **NOTE:** This is a stand-alone console release. You can download the console [here](https://my.datomic.com/downloads/console).

## 1.0.6165: 2020/05/14

- New feature: [qseq](https://docs.datomic.com/query/query-executing.html#qseq).
- New feature:[index-pull](https://docs.datomic.com/query/query-index-pull.html).
- New feature: [pull xform](https://docs.datomic.com/query/query-pull.html#xform).
- Upgrade: the transactor and peer now require Java 1.8 or greater.
- Enhancement: optimize cache usage of queries that pull.
- Enhancement: improve index efficiency.
- Fix: only read \*.edn metaschema files in analytics support.
- Updated REST server jetty dependencies from 9.4.24.v20191120 to 9.4.27.v20200227.
- Updated liberator (used in REST server) to 0.15.3.

## 0.9.57: 2020/05/14 - Client-Pro Update

- New feature: [qseq](https://docs.datomic.com/query/query-executing.html#qseq).
- New feature: [index-pull](https://docs.datomic.com/query/query-index-pull.html).
- Updated jetty dependencies from 9.4.24.v20191120 to 9.4.27.v20200227.

## 0.9.6045: 2020/02/13

- New feature: `[:db/retract eid aid]` will retract all values for an eid/aid combination.
- [Critical fix](https://docs.datomic.com/release-notices.html#0.9.6045): regression introduced in 0.9.6014 where `datoms`, `seek-datoms`, and `index-range` return incorrect results if `Iterable.iterator` is called more than once on the returned value.
- Fix: respect `nil` as a limit in query `pull` expression.
- Fix: respect `false` as a default in query `pull` expressions for boolean-valued attributes.
- Fix: allow arbitrary `java.util.List` (not just Clojure vectors) for lookup refs.
- Updated REST server jetty dependencies from 9.4.15.v20190215 to 9.4.24.v20191120.
- Updated bundled Presto server from 316 to 329.
- Removed Groovy from the transactor distribution. This was unused except for examples.

## 0.9.6024: 2020/01/14

- Eliminate unnecessary peer dependency on core.async and tools.reader introduced in 0.9.6014.
- Fix: `#db/fn` literals preserve `:code` metadata.

## 0.9.6021: 2020/01/08

- Fix: prevent a rare scenario where retracting non-existent entities could prevent future transactions from succeeding.
- Update transactor core.async dependency to 0.5.527.

## 0.9.6014: 2019/12/13

- Enhancement: performance improvement for range predicates in analytics support.
- Enhancement: many error maps now include the tempid mappings for data that caused a transaction to fail.
- Enhancement: when the transactor is unavailable, the peer returns an error including a `:cognitect.anomalies/unavailable` category.
- Fix: improve performance of lookup refs against as-of databases.
- Fix: include bash shebang in `bin/maven-install`.
- Fix: report an error on invalid aggregate function names in query.

## 0.9.5981: 2019/10/21

- New: `index-parallelism` transactor property enables higher index job throughput.

[Index Parallelism](https://docs.datomic.com/operation/capacity.html#index-parallelism).

- Fix: prevent exception thrown when handling COUNT(\*) clauses in analytics support.
- Fix: resolve tempids for reference attributes inside tuples.
- Fix: do not create redundant `:db.install/attribute` datoms for idempotent schema operations.
- Update transactor Clojure dependency to 1.10.1.

## 0.9.5966: 2019/10/01

- New feature preview: [analytics](https://docs.datomic.com/analytics/analytics-concepts.html).
- Updated AWS library dependency to 1.11.600.
- Expanded the retry window for backup and restore operations. This helps backup and restore jobs completely in the face of transient errors.
- Stopped reporting transaction errors asynchronously.

## 0.9.5951: 2019/07/31

- Redact password in exception data when peer connection fails.
- Correctly handle boolean attributes in composite tuples.
- Updates to address vulnerabilities in Apache dependencies:
	- Jackson from 2.6.6 to 2.9.9.
		- Logback from 1.0.13 to 1.2.0.
		- Zookeeper 3.4.6 to 3.5.5.

## 0.9.5930: 2019/07/02

Fixed problem where databases that change an attribute from `:db.cardinality/one` to~:db.cardinality/many~ may become unavailable after a process restart.

## 0.9.5927: 2019/06/28

- New feature: [Tuples](https://docs.datomic.com/schema/schema.html#tuples).
- New feature: [attribute predicates](https://docs.datomic.com/schema/schema.html#attribute-predicates).
- New feature: [entity specs](https://docs.datomic.com/schema/schema.html#entity-specs).
- New feature: [return maps](https://docs.datomic.com/query/query-data-reference.html#return-maps).
- New feature: [Cassandra2](https://docs.datomic.com/operation/storage.html#cassandra2).
- Vulnerability Fix: Updated [commons Collections](https://issues.apache.org/jira/browse/COLLECTIONS-580) from 3.2.1 to 3.2.2.
- Fix: `sample` aggregate could hang the query thread.
- Improved performance of Valcache cleanup.
- Updated REST server jetty dependencies from 9.3.7.v20160115 to 9.4.15.v20190215.
- Updated ActiveMQ Artemis dependencies from 1.4.0 to 1.5.6.

## 0.9.5786 2018/11/15

Fix: re-enable memcached support which was inadvertently disabled in 0.9.5783.

## 0.8.28: 2018/11/27 - Client-Pro Update

- Add compatibility with com.cognitect/aws-api.
- Add Datafy.
- Improved error reporting.
- Upgraded http-client to 0.1.87.

## 0.9.5783: 2018/10/10

- New feature: [Valcache](https://docs.datomic.com/operation/valcache.html).
- Fixed cache problem in peer-server where all `d/with` databases deriving from a common initial call to `d/with-db` had the same common value.
- Fixed bug introduced in 0.9.5703 that prevented using class static methods in query function expressions.

## 0.8.20: 2018/08/21 - Client-pro update

- Bugfix: fixed Jetty configuration that could cause a client to prevent JVM from shutting down.
- Upgraded transit-clj to 0.8.313.

## 0.8.17: 2018/07/02 - Client-pro update

Enhancement: added sync to Client API. [Client Synchronization](https://docs.datomic.com/transactions/client-synchronization.html).

## 0.9.5703 2018/06/06

New feature: [classpath functions](https://docs.datomic.com/transactions/transaction-functions.html#types).

## 0.9.5697 2018/03/29

- Release 0.9.5697 fixes two [security vulnerabilities](https://forum.datomic.com/t/important-security-update-0-9-5697/379) in Datomic On-Prem transactors running the free: or dev: storage protocol.
- Enhancement: new security configuration options for free: and dev: transactors.

## 0.9.5661 2018/01/11

- Upgrade: updated Transactor AMIs for all regions to the latest Amazon Linux. Note that per AWS guidelines PV AMIs are no longer provided.
- Added support for eu-west-3 region.

## 0.9.5656 2017/12/05

New feature: `:as` and `attr-with-opts` syntax in [the pull API](https://docs.datomic.com/query/query-pull.html).

## 0.9.5651 2017/11/29

- Enhancement: peer server allows arbitrary code in queries.
- Enhancement: better error messages from peer server.

## 0.9.5561.62 2017/10/06

- Bugfix: transactors and peers now use disjoint ranges for tempid generation, so tempids generated in transaction functions cannot collide with tempids generated on the peer.
- Bugfix: fixed a bug where starting a peer and transactor at about the same time could lead to a string tempid incorrectly unifying with another tempid in the same transaction.

## 0.9.5561.59 2017/09/20

Bugfix: fixed bug that prevented health check endpoint from starting on machines with more than 8 cores.

## 0.9.5561.56 2017/08/16

Bugfix: stop printing sensitive Cassandra connection information in the log.

## 0.9.5561.54 2017/07/24

- Bugfix: fixed an HA performance bug where a transactor could continue to write a heartbeat during the shutdown, causing up to 30 second delay before the standby transactor could take over.
- Enhancement: more efficient index use in transaction processing. This will improve write latency and throughput for some update-heavy transaction loads.

## 0.9.5561.50 2017/06/07

- Bugfix: release 0.9.5561.50 fixes a bug in the catalog that, in the unlikely circumstance where one has deleted a database and restored it from a backup without first having called `gc-deleted-dbs`, can cause a subsequent `gc-deleted-dbs` to delete that (active) database.
- Bugfix: prevent unbounded thread use by query pool.
- Upgrade: peers and transactors now use version 1.11.82 of the AWS SDK.
- Upgrade: updated the AWS regions and instance types available via the CloudFormation template.
- Enhancement: better error message when unable to resolve an entity.
- Enhancement: health check endpoints for transactors and peer servers.

## 0.9.5561 2017/02/13

Bugfix: fixed a bug where the peer server could ignore the `basis-t` of a client request, answering queries from the latest db value instead.

## 0.9.5554 2017/01/23

- Bugfix: fixed bug where queries with variables in attribute position could return tuples that differ only by Java representation, e.g. a tuple with Integer 1 and a 'different' tuple with Long 1.
- Bugfix: fixed a bug where an ident that has been used to name more than one different entity over time could return an outdated entity id from e.g. `d/entid`.
- Improvement: better performance in creating memory databases.

## 0.9.5544 2016/12/06

Bugfixes in new functionality introduced in 0.9.5530:

- Clients can now query values produced by `with-db`.
- String tempids are now available in the `:tempids` key returned by e.g. `transact`.
- Indexing jobs correctly handles the implicit use of `:db.install/attribute` and `:db.alter/attribute`.

## 0.9.5407 2016/10/28

- Bugfix: fixed bug introduced in 0.9.5404 where peers do not reconnect after losing the transactor connection until the peer makes another call to the transactor.
- Performance improvement for queries where AVET is more selective than VAET.

## 0.9.5404 2016/09/28

- Upgrade: the transactors and the peer now work with Cassandra 3.1.0 driver.
- Upgrade: the transactor and the peer now use ActiveMQ Artemis 1.4.0. Peers from this release forward -cannot- connect to older transactors. When upgrading to (or past) this release, you must upgrade transactors first.

## 0.9.5394 2016/08/15

- Bugfix: eliminated a bug where given a particular and extremely unlikely interleaving of indexing failures and transactor successions, some transactions can be incorporated into the log but not in the indexes.
- Bugfix: `:db.fn/cas` now rejects attemps to CAS a cardinality-many attribute.

## 0.9.5390 2016/08/03

- Improvement: the `log` API can now be used with the memory database.
- Bugfix: a unique attribute value can now be transferred from one entity to another within a transaction.

## 0.9.5385 2016/06/29

- Upgrade: the transactor and peer now use HornetQ 2.4.7. Peers from this release forward -cannot- connect to older transactors. When upgrading to (or past) this release, you must upgrade transactors first.
- Upgrade: Datomic now uses AWS SDK 1.11.6.
- Bugfix: substantially improved the performance of a corner case involving transactions with a large number of retractions.
- Bugfix: fixed bug where AVET index could become temporarily unavailable on a recently added attribute.
- Bugfix: Clojure begins now work in transaction FNS.

## 0.9.5372 2016/05/31

- Upgrade: the transactor and peer now require Clojure 1.8.0 or greater.
- Bugfix: prevent a scenario where not all predicates were applied in queries with a `not` clause.
- Bugfix: respect `default` specifications in pull expressions containing a wildcard.
- Bugfix: allow `isComponent` only on ref types in new data, and ignore meaningless `isComponent` specifications on non-ref types in existing data.

## 0.9.5359 2016/04/25

- Improvement: all Datomic command line tools now use the standard AWS credentials provider.
- Bugfix: improved performance of pull + wildcard + since database.
- Bugfix: fixed bug that could in rare cases cause a peer to be unable to reconnect to a transactor after a transient failure.
- Bugfix: eliminated a few spurious maven dependencies from the Peer library.

## 0.9.5350 2016/02/09

- Improvement: connection caching behavior has been changed so that peers can now connect to the same database served by two (or more) different transactors.
- Bugfix: it is no longer possible to create a database name that will result in an invalid URI.

## 0.9.5344 2015/12/03

- Notice: the full Amazon AWS SDK for Java is no longer shipped with the Datomic peer library. If you are using AWS, see the section in the storage docs about including the AWS SDK for Java for help with configuring your peer with this upgrade: [provisioning-dynamo](https://docs.datomic.com/operation/storage.html#provisioning-dynamo).
- New: the transactor and peer now require Clojure 1.7.0 or greater.
- Improvement: remove dependency on monolithic AWS SDK for Java library.
- Improvement: add support for eu-central-1.
- Improvement: throw error when `datomic.api/pull` is passed a history db.
- Bugfix: fixed bug that could cause a transactor to report a spurious error message after a successful restore if the transactor wasn't running.
- Bugfix: fixed bug that could, in unusual circumstances, cause a database to fail to load with an NPE in datomic.db-next\_valid\_inst

## 0.9.5327 2015/10/13

- Bugfix: fixed bug that could cause a transactor to terminate when deleting a database against Cassandra's storage.
- Bugfix: fixed bug where retracting an excision could cause indexing to fail.
- Bugfix: fixed bug that prevented connecting from a peer that deletes and recreates a database name.

## 0.9.5302 2015/09/17

- Bugfix: eliminated a bug where given a particular and extremely unlikely interleaving of indexing failures and transactor successions, some transactions can be incorporated into the log but not in the indexes.
- Bugfix: improved storage garbage tracking.
- Fixed bug that could cause processes using Cassandra driver to hang during peer shutdown.

## 0.9.5206 2015/07/30

Bugfix: Document and enforce [limitations](https://docs.datomic.com/schema/schema.html#bytes-limitations) of `:db.type/bytes`.

## 0.9.5201 2015/07/16

Reduced memory and thread use in DDB storage when writing large segments.

## 0.9.5198 2015/07/06

- Fixed bug where dropping and re-adding index on an attribute could lead to retracted values reappearing in the AVET index.
- Fixed bug where in rare cases the log API could be temporarily unable to see the most recent transaction.
- Fixed bug where the pull API did not always return all explicit reverse references.

## 0.9.5186 2015/06/18

- Improvement: better error reporting when an invalid entity id is transacted.
- Improvement: work around a retry bug in Cassandra that can cause unnecessary failures, check [this document](https://datastax-oss.atlassian.net/browse/JAVA-764).
- Improvement: preserve ordering of `:db/txInstant`: If the transactor's system clock returns a value older than the time of the most recent tx, use the most recent tx's instant instead.
- Updated to aws-java-sdk 1.9.39.
- Updated to com.datastax.cassandra/cassandra-driver-core to 2.1.5.
- Fixed bug in Pull API when using selectors built from strings.
- Fixed bug that prevented backup/restores from working in the previous datomic-free release.

## 0.9.5173 2015/05/12

- Improvement: getting log value from a connection is now much faster.
- Improvement: `restore-db` now prints the basis of the restored database.
- Improvement: metrics can now be enabled during backup/restore.
- Fixed bug: `:db.fn/retractEntity` no longer throws an exception when passed an invalid entity identifier.
- Fixed bug where `syncIndex` API future could fail to complete.

## 0.9.5153 2015/03/19

- New feature: [query timeout](https://docs.datomic.com/query/query-data-reference.html).
- Improvement: the datalog engine will now do self-unification within a single clause.
- Improvement: better semantics for query function `/`: [built-in-expressions](https://docs.datomic.com/query/query-data-reference.html).
- Improvement: better error reporting when a query attempts to use a non-attribute as an attribute.
- Fixed a bug that could cause a recursive query to return an undersized result.
- Fixed a bug that could cause restore to fail on a case-sensitive file system.
- Fixed bug that could throw a Null Pointer Exception when calling API functions on nonexistent identities.

## 0.9.5130 2015/01/13

- Note: we are looking for feedback on this initial release of `not` clauses and `or` clauses described below. The API is subject to change based on this feedback. See [this blog post](https://blog.datomic.com/2015/01/datalog-enhancements.html) for an overview of the new features.
- New feature: [`not` clauses](https://docs.datomic.com/query/query-data-reference.html#not-clauses).
- New feature: [`or` clauses](https://docs.datomic.com/query/query-data-reference.html#or-clauses).
- New feature: require vars [in rules](https://docs.datomic.com/query/query-data-reference.html#basics).
- Improvement: backing up to the same storage is now differential.
- Performance Enhancement: the query engine will make better use of AVET indexes when range predicates are used in the query.
- Improvement: `Peer.getDatabaseNames` can accept a map for Cassandra and SQL storages: [getDatabaseNames](https://docs.datomic.com/javadoc/datomic/Peer.html#getDatabaseNames-java.lang.Object-).
- Improvement: better error messages from invalid transactions.
- Update com.datastax.cassandra/cassandra-driver-core to 2.0.8.

## 0.9.5078 2014/11/25

- New CloudWatch metrics: `WriterMemcachedPutMusec`, `WriterMemcachedPutFailedMusec` `ReaderMemcachedPutMusec` and `ReaderMemcachedPutFailedMusec` track writes to [Memcache](https://docs.datomic.com/operation/caching.html#memcached).
- Improvement: better startup performance for databases using fulltext.
- Improvement: enhanced the Getting Started examples to include the Pull API and find specifications.
- Improvement: better scheduling of indexing jobs during bursty transaction volumes.
- Fixed bug where Pull API could incorrectly return renamed attributes.
- Fixed bug that caused `db.fn/cas` to throw an exception when `false` was passed as the new value.

## 0.9.5067 2014/11/07

- Improvement: stronger validation on schema value types and cardinalities.
- Improvement: better error logging for Cassandra.
- Fixed a bug that could cause an exception when using limits in a recursive pull specification.

## 0.9.5052 2014/10/28

- New feature:[pull](https://blog.datomic.com/2014/10/datomic-pull.html). Check this [pull](https://docs.datomic.com/query/query-pull.html) documentation.
- New feature: [query find specifications](https://docs.datomic.com/query/query-data-reference.html#find-specifications).
- New feature: [query pull expressions](https://docs.datomic.com/query/query-data-reference.html#pull-expressions).
- New: [`Peer.getDatabaseNames`](https://docs.datomic.com/javadoc/datomic/Peer.html#getDatabaseNames-java.lang.Object-).
- New: [peer custom monitoring](https://docs.datomic.com/operation/monitoring.html#custom).
- Fixed bug that could cause `Peer.shutdown` to hang.
- Fixed bug that could cause `Peer.createDatabase` to leak threads.
- Fixed bug where nested queries could deadlock.
- Downgraded groovy-all dependency to 1.8.9

## 0.9.4956 2014/10/10

- Notice: PostgreSQL is no longer shipped with the Datomic peer library. If you are using PostgreSQL, see the section in the storage docs about JDBC drivers for help with configuring your peer with this upgrade: [sql-database](https://docs.datomic.com/operation/storage.html#sql-database).
- New: `cassandra-cluster-callback` transactor property for configuring a [Cassandra](https://docs.datomic.com/operation/storage.html#cassandra) cluster.
- New: [garbage collection](https://docs.datomic.com/operation/capacity.html#garbage-collection-deleted) for deleted databases.
- New: transactions now support Clojure bigint literals.
- New: `groovysh.cmd` for Windows.
- Improvement: better failover handling for high availability.
- Improvement: better handling of intra-transaction unique identity violations.
- Improvement: provisioning a new Cloud Formation is now idempotent in an AWS VPC.
- Improvement: `Database.attribute` now returns nil for non-existent attributes.
- Improvement: peers no longer try to reconnect to deleted databases.
- Updated peer library dependencies to the following versions: aws-java-sdk: 1.8.11 cassandra-driver-core: 2.0.6 curator-framework: 2.6.0 groovy-all: 2.3.6 guava: 18.0 postgresql: 9.3.1102 JDBC 4.1 slf4j: 1.7.7 spymemcached: 2.11.4
- Fixed bug that caused DynamoDB Local URIs to ignore AWS credentials.
- Fixed bug that could cause `Database.id` to throw an exception against a Cassandra storage.
- Fixed a bug that could cause process to hang during peer shutdown.

## 0.9.4899 2014/09/09

- New: the transactor and peer now require Clojure 1.6.0 or greater.
- Fixed a bug that could cause a transactor to become unresponsive after waking from laptop sleep.

## 0.9.4894 2014/08/26

- New: transactor configuration setting to disable printing of credentials. Specify the boolean system property `datomic.printConnectionInfo` as `false` to disable. The default is true.
	Transactor properties documentation can be found at [System Properties](https://docs.datomic.com/operation/system-properties.html).
- Fixed bug that prevented nested map `db/id` overrides with string keys.
- Fixed a bug that could cause extra sessions to be created against Cassandra storage.
- This release optimizes the repair job introduced in 0.9.4880 to minimize its impact on live systems.

## 0.9.4880 2014/08/21

- Fixed bug where some indexed retractions are not enforced. This can cause old values to reappear, or uniqueness constraints to consider retracted values. Upgrading the transactor to this release will repair the problem during the next indexing job.
- Delay accepting V-only bindings in query ordering.
- Provide better error messages when backup/restore directory does not exist.
- Upgraded cassandra-driver-core to 2.0.3. This update fixes a bug in Cassandra that was causing it to leak file descriptors as described [here](https://issues.apache.org/jira/browse/CASSANDRA-6275).
- Added more logging around keys being written to storage.
- Added an error message when peers see no heartbeat.
- Added better error message when the value associated with the key is unavailable to be read from storage.
- Fixed bug where processes would sometimes prefer an old ident over a new one after a restart.
- SQL storage users can now configure the pool validation query via `datomic.sqlValidationQuery`, default is "SELECT 1".
- Re-enable metrics that were inadvertently disabled in 0.9.4815, e.g. `StorageGetMsec` and `StoragePutMsec`.
- Integrity validations added in newer versions of Datomic no longer prevent loading older non-compliant databases.

## 0.9.4815 2014/08/13

- Transactors now use the G1 garbage collector and require Java 7 or later. Peers continue to work with Java 6 or later.
- Database backups to S3 can now use '-encryption sse' to request server-side encryption.
- Better defaults and more configuration options for I/O utilization during backup and restore. [The Defaults](https://docs.datomic.com/operation/backup.html#performance) are now good for most scenarios.
- Updated AMIs across all regions with current Amazon Linux distributions and support for HVM-based instances.
- Added support for more Amazon instance types.
- Prevented race condition in `deleteDatabase` that could tie up a thread writing error messages in the log.
- Fixed bug where peers that were offline during a transaction would not see fulltext for the offline time until the next indexing job was completed.
- Better protection against retracting schema entities. If you encounter the following error:
	java.lang.IllegalArgumentException::db.error/invalid-install-attribute at datomic.error-arg.invoke(error.clj:55) at datomic.db-install\_attribute\_hook.invoke(db.clj:1034) at datomic.db-run\_hooks-fn\_\_3689.invoke(db.clj:1849)
	then you may have incorrectly retracted a schema entity. Contact support@cognitect.com for assistance recovering from this error.
- Protection against placing non-schema entities in the `:db.part/db` partition. If you see the following error message
	"Only schema components can be installed in partition:db.part/db"
	you will need to correct your program to create non-schema entities in `:db.part/user` or a partition you create.
- Fixed bug that prevented compilation of some `:require` forms in data functions.

## 0.9.4766 2014/05/21

- New built-in [query expressions](https://docs.datomic.com/query/query-data-reference.html#expression-clauses): `get-else`, `get-some`, `ground`, and `missing`.
- Added support for memcached-sasl, check [Caching](https://docs.datomic.com/operation/caching.html) for details.
- Allow lookup refs for V position in users of VAET index, including `:db.fn/retractEntity`.
- String representation for Connection no longer throws an exception if the connection is closed.

## 0.9.4755 2014/04/23

Re-enable input-bound rule predicates

## 0.9.4752 2014/04/22

- Fixes for parallelism in query function expressions.
- Fixes variable-less entity clauses.

## 0.9.4745 2014/04/19

Fixed bug where systems using adaptive indexing could become unavailable with the following exception on both transactors and peers:

java.lang.NullPointerException: null at datomic.db-find\_last\_tx.invoke(db.clj:1875) at datomic.db-db.invoke(db.clj:1885)

All users of adaptive indexing (0.9.4699 or greater) are strongly encouraged to upgrade to this release. Both transactors and peers need to be upgraded, but it's not necessary to upgrade them simultaneously.

## 0.9.4724 2014/04/16

This release upgrades the backup format to improve backup capacity against large databases. Datomic now stores backups using a subdirectory structure.

0.9.4724 can restore backups taken by previous versions of Datomic. However, previous versions of Datomic will not be able to restore newer backups.

## 0.9.4718 2014/04/15

More query optimizations. Keeps single-valued:in clauses at top.

## 0.9.4714 2014/04/14

Query optimizations.

## 0.9.4707 2014/04/08

- Fixed bug in peer object cache management that could result in OOM errors with large (\> 2GB) caches.
- Better error reporting for storage failures.
- Allow configuration of transactor KeyStore and TrustStore to facilitate encrypted communication with [Storages](https://docs.datomic.com/operation/storage.html).

## 0.9.4699 2014/03/24

- New feature: [adaptive indexing](https://blog.datomic.com/2014/03/datomic-adaptive-indexing.html). Check [upgrading](https://docs.datomic.com/release-notices.html).
- Renamed metric: `AlarmTxStalledIndexing` is now `AlarmBackPressure`.
- Fixed default region in CloudFormation template.

## 0.9.4609 2014/04/08

- Upgraded HornetQ dependency to 2.3.17.Final. Due to changes in HornetQ, this release is incompatible with previous releases of Datomic. Both peers and transactors will need to be upgraded together.
- Updated Guava dependency to 16.0.1.

## 0.9.4578 2014/03/24

- Fixed bug where adding:db/index sometimes would not take effect. If you encounter this error, upgrade to this release then drop and read the index.
- Tighter validation around invalid schema changes.

## 0.9.4572 2014/03/13

- Prevent out-of-memory errors with very large indexing jobs, particularly those that are adding new AVET indexes.
- Release JDBC resources more aggressively when using SQL storage.

## 0.9.4556 2014/03/05

- New feature: [lookup refs](https://blog.datomic.com/2014/02/datomic-lookup-refs.html).
- Fixed bug where the transaction tempid would not resolve correctly when used only in value position.
- Fixed bug where peers could leak resources trying to connect to databases that have been deleted.
- More detailed error messages for some invalid transactions.
- Raise the enforced level of the total number of schema elements (such as attributes, partitions, and types) to be fewer than 2^20

---

0.9.4532 includes an upgrade to the log format that requires simultaneous update of peers and transactors, and is not compatible with older versions, see below:

## 0.9.4532 2014/02/24

This release upgrades the database log to format version 2.

- All peers and transactors in a system must move together to this version or later.
- The transactor and peers are backward compatible with log version 1 Transactors will automatically upgrade database logs to version 2 when a database is used. The performance cost of this upgrade is negligible and upgrades can be performed against production systems.
- Older versions of the peer and transactor are -not- forward compatible with log version 2, and will report the following error:
	NullPointerException java.util.UUID.fromString
- Downgrading to log version 1 is possible. This release includes a command line tool that can be used to downgrade a database to log version 1:
	bin/datomic revert-to-log-version-1 {your-database-uri}
- Fixed bug where, after a transactor failure, Datomic processes could fail to start with a "Gap in data" exception.
- Fixed performance problem with *variance* and *sttdev* query aggregation functions.
- Limited thread use in query.

## 0.9.4497 2014/02/07

- Improvements to the [REST](https://docs.datomic.com/reference/rest.html) client.
- Added a GettingStarted.groovy sample in the samples/seattle directory.
- Added SSL support for [Riak and Cassandra](https://docs.datomic.com/operation/storage.html).
- Improved script for launching transactor to allow for cleaner process management.
- Improved built-in Compare-and-Swap performance.
- Datomic now enforces the total number of schema elements (such as attributes, partitions, and types) to be fewer than 32k.

## 0.9.4470 2014/01/29

- New Feature: [alter schema](https://blog.datomic.com/2014/01/schema-alteration.html). This feature breaks compatibility with older versions. Once a schema alteration has been performed on a database, only connect peers and transactors running at least version 0.9.4470 to that database.
- New API: database.[attribute](https://docs.datomic.com/javadoc/datomic/Database.html#attribute-java.lang.Object-) and [Attribute](https://docs.datomic.com/javadoc/datomic/Attribute.html).
- New APIs: [connection.syncIndex / syncSchema / syncExcise](https://docs.datomic.com/javadoc/datomic/Connection.html).
- Datomic will now reject some unworkable memory settings, e.g. combinations of memory-index-max and object-cache-max that exceed 75% of JVM RAM.
- Added validation to prevent conflicting unique value assertions within a transaction.
- Fixed bug that prevented Datomic Console from working with Riak or Cassandra storage.
- Better logging for Riak storage.

## 0.9.4384 2014/01/23

Preliminary support for Cassandra [Storage](https://docs.datomic.com/operation/storage.html).

## 0.9.4360 2013/12/18

Used jarjar to move Datomic's use of Apache Lucene to package names that will not conflict with the application use of Lucene.

## 0.9.4353 2013/12/12

- Updated to aws-java-sdk 1.6.6.
- Datomic now supports DynamoDB Local as a development-time [Storage](https://docs.datomic.com/operation/storage.html) option.
- Datomic transactions now validate that tempids have a valid partition.
- Fixed bug where some transactions would timeout unnecessarily, even though the work was completed by the transactor.
- Fixed bug where fulltext queries did not find answers in the history database.

## 0.9.4331 2013/12/09

- Made transactor more robust against transient SQL storage connection failures.
- Fixed bug where transactor would log a spurious NPE from datomic.hornet during shutdown.

## 0.9.4324 2013/11/27

- Fixed a bug in IAM roles support that prevented automatic renewal of role credentials in one case.
- Fixed a bug in the build process that caused the 0.9.4314 version of the Console to be non-operational.

## 0.9.4314 2013/11/25

- IAM roles are now the preferred way to supply credentials when running Datomic in AWS. See [Storage](https://docs.datomic.com/operation/storage.html) to configure a new transactor using IAM roles. Check [migrate-to-roles](https://docs.datomic.com/legacy/migrate-to-roles.html) to migrate an existing Datomic configuration to use IAM roles.
- The transactor now supports a mechanism for integrating different monitoring services, see the 'Custom Monitoring' section of [monitoring](https://docs.datomic.com/operation/monitoring.html).
- Updated Riak client to 1.4.2.
- Cloudwatch and custom monitoring now report the count of `RemotePeers`.

## 0.8.4270 2013/11/18

- Fixed bug where large excisions could prevent subsequent storage garbage collection.
- Fixed bug where Datomic Pro eval keys did not have access to HA.
- Datomic now verifies Lucene major version without using package reflection (fixes inability to uberjar 4260).
- Updated Fressian dependency to 0.6.5.
- Better error message on nil input to query.

## 0.8.4260 2013/11/07

- Make sure Util.read preloads schema-reading helpers, so that schemas can be read before touching a database.
- Fixed bug in connection pool validator for Oracle SQL storage.

## 0.8.4254 2013/10/29

- New: [Datomic Console](https://docs.datomic.com/resources/console.html).
- More consistent use of [error handling](https://docs.datomic.com/api/error-handling.html) throughout API.
- Quicker detection of transactor disconnects, reported to peers via exceptions in transaction futures and sync futures.
- Fixed bug in 4215, 4218 that caused `IllegalStateException` when calling `Util.read`.
- Corrected sa-east-1 transactor AMI ID.

## 0.8.4218 2013/10/11

Fixed bug where transaction promises could be garbage collected before a transaction completes, preventing `ListenableFuture` callbacks from firing.

## 0.8.4215 2013/10/01

Improved redundancy elimination. If a transaction presents a datom that is identical (except for tx) to an existing datom, that datom will not be added to the log.

## 0.8.4159 2013/09/10

- Updated aws-java-sdk dependency to 1.5.5, discontinued using jets3t.
- Reduced background thread usage in query.
- Transactions now convert doubles to floats when attribute schema requires floats.
- Transactor heartbeat is now more robust in the face of transient failures writing to storage.

## 0.8.4143 2013/08/22

- `:db/noHistory` is now respected by indexer.
- More precise docstring for `:db/noHistory`.

## 0.8.4138 2013/08/14

- Improved transactor performance, specifically around memory usage and GC pauses. This includes improved memory efficiency in indexing, and a new set of GC flags in bin/transactor.
- Fixed bug where queries with constants in V position could return too many results.
- Transactor does more useful logging at the INFO level, so there should be less need to modify the logging configuration.

## 0.8.4122 2013/08/03

- New: API for accessing the [log](https://docs.datomic.com/reference/log.html).
- Return correct result from REST avet datoms call when neither start nor end are provided.
- Eliminate spurious file bin/logback-test.xml that was incorrectly included in 0.8.4111.

## 0.8.4111 2013/07/31

- New: memory settings for the transactor must be explicitly specified in the transactor properties file. There are no default settings, so existing transactor property files that lack these settings will no longer work. The transactor properties sample files include three complete examples, representing ongoing usage, one-time imports, and constrained-memory usage during development.
- New: stricter validation of datoms within a single transaction, preventing duplication of the same datom and multiple assertions of cardinality-one attributes. This will cause transactions that would formerly have succeeded to fail with a validation error.
- New: the transactor and peer now require Clojure 1.5.1 or greater.
- New: peer configuration setting for peer/transactor connection timeout. Specify system property `datomic.peerConnectionTTLMsec` in milliseconds. Default and minimum are both 10000.
- New: Reverse attribute lookups from an entity are returned as sets.
- Transactors are now more robust in the face of transient errors writing to storage.
- `touch` now works correctly for component entities that have `:db/ident`.
- Better error messages for invalid S3 backup URL, attempting to back up a database that does not exist.
- Updated jetty dependencies (used in REST server).

## 0.8.4020.26 2013/07/09

- Fixed bug that prevented zero-argument data functions in Java.
- Fixed bug where peers have trouble connecting when already-connected peers are producing a heavy write load.

## 0.8.4020.24 2013/07/01

- Fixed bug in 0.8.4020 where nested collections of preexisting entity ids were incorrectly rejected by the transactor, with a "Unable to interpret … as a reference target" message.
- Fixed bug in 0.8.4020 where use of a transaction entity in value position only leads to background indexing failure on the transactor.

## 0.8.4020 2013/06/19

- [Component entities can now be created as nested maps in transaction data](https://blog.datomic.com/2013/06/component-entities.html).
- Tightened peer HornetQ timeout for faster failover.
- Allow sets as an alternative to lists when specifying values for a cardinality-many attribute in transaction data.
- Fixed bug: Allow the transaction entity to appear in value position only in transaction data.
- Fixed bug: Prevent excessive memory use that would occur before rejecting an invalid transaction that attempts to `db.fn/retractEntity` nil.

## 0.8.4007 2013/06/12

- Updated several library dependencies to more recent versions: aws, guava, h2, logback, netty, and slf4j.
- Better `toString` representations for objects likely to be encountered in shell (e.g. groovysh) sessions.

## 0.8.3993 2013/06/03

- Increased parallelism in query.
- New API: [connection.sync](https://blog.datomic.com/2013/06/sync.html.).
- Connection.db now returns a database immediately, even if the transactor is unavailable.
- Improved peer recovery time during [HA](https://docs.datomic.com/operation/ha.html) failover.

## 0.8.3971 2013/05/23

- Fixed bug where some active log segments are marked as garbage, which can result in log corruption after a call to `gcStorage`. Moving to this release is strongly advised.
- Improved CloudWatch metric: `IndexWrites` is now reported once per indexing job, making it easier to reason about per-index-job load.
- New CloudWatch metric: `TransactionBatch` tracks the number of transactions batched into a single write to the log.
- New CloudWatch metric: `MemoryIndexFillMsec` reports an estimate of the time to fill the memory index, given the current write load.

## 0.8.3970 2013/05/23

(Do not use this release).

## 0.8.3960 2013/05/20

Fixed a bug where indexing jobs could fail for fulltext attributes.

## 0.8.3952 2013/05/16

- Fixed bug where HA transactor pool can become stuck and no transactor can start. All HA users should adopt this release as soon as possible.
- Mark log segments as garbage after excision. Note that.gcStorage is still a necessary separate step.
- Improved efficiency in writing transaction logs.
- Quickly report pending transactions failed if the transactor connection is lost.

## 0.8.3941 2013/05/10

[Excision](https://docs.datomic.com/operation/excision.html).

## 0.8.3899 2013/05/08

Fixed bug where background index creation could fail to trigger in systems running multiple databases, eventually resulting in the need to restart transactor.

## 0.8.3895 2013/04/26

- Allow explicit `h2-port` and `h2-web-port` specification in the query string for free and dev protocols.
- Fixed bug where Hornet usage of file system did not respect `data-dir` transactor property.
- Upgraded to netty 3.6.3.Final.

## 0.8.3889 2013/04/14

- Enhanced SQL storage to work with a wider set of databases, including Oracle.
- Fixed bug where, after a cold restart of transactor, peer could attempt transactions that would arrive before the transactor was ready and timeout.
- Fixed bug that caused "Database deleted" error when deleting and recreating databases in development.
- Fixed bug in CloudFormation template creation that required explicit setting of `java-opts` property, even when defaults acceptable.
- Added missing AWS instance sizes to CloudFormation template generator.
- Upgraded to AWS Java SDK 1.4.1.

## 0.8.3862 2013/03/29

Fixed incorrect validation introduced in 0.8.3861 that prevented asserting the attribute value `false`.

## 0.8.3861 2013/03/27

- New API `Peer.shutdown`.
- New API `Connection.release`.
- Fixed spurious error after heartbeat failure on transactor.
- New: positional destructuring of datoms (Clojure API).
- Fixed performance problems in `:db.fn/retractEntity`.

## 0.8.3848 2013/03/13

- Fixed `Entity.keySet` to return a set of strings.
- Better naming convention for log rotation: `{bucket}/{system-root}/{status}/{time-status-reached}`, where status is "active" or "standby".

## 0.8.3843 2013/03/12

- Fixed bug where invalid (non-string) fulltext attribute prevents log ingest.
- New `seekDatoms` and `entidAt` APIs.
- `Heartbeat` and `HeartMonitor` metrics have been replaced by the more informative `HeartbeatMsec` and `HeartMonitorMsec` metrics.
- New `java-opts` option in transactor properties.

## 0.8.3826 2013/02/25

- Improved resilience to transient errors in backup/restore.
- Upgrade to netty 3.6.0.Final, fixing SSL race condition that could prevent peers from connecting.
- New `java-xmx` setting in CloudFormation template makes JVM heap configuration more evident.
- Deprecated `StorageBackoff` metric in favor of a more explicit `StorageGetBackoffMsec` and `StoragePutBackoffMsec` metrics.

## 0.8.3814 2013/02/14

- Prevent aggressive timeout in Peer.connect that could occur when connecting to cold transactor + slow log ingest.
- Allowed peers to ingest log in parallel with ingest on a cold transactor.
- Added LogIngestMsec and LogIngestBytes Cloudwatch metrics. See [monitoring](https://docs.datomic.com/operation/monitoring.html) for more on metrics.
- Renamed system property `datomic.objectCacheBytes` to `datomic.objectCacheMax` for consistency with other property names. See [capacity](https://docs.datomic.com/operation/capacity.html) for more on capacity-related properties.

## 0.8.3803 2013/02/12

- Fixed bug that caused intermittent deadlock communicating with storage.
- Reduced memory usage during backup and restore.

## 0.8.3789 2013/02/02

- Made default JVM heap sizes for AMI appliances more conservative.
- Set transactor instance name based on CloudFormation template name.
- Include command-line utility JARs that were inadvertently omitted in 0.8.3784.

## 0.8.3784 2013/02/01

- Improved indexing performance: lower memory use on the transactor, and higher transaction throughput during indexing jobs.
- New [deployment documentation](https://docs.datomic.com/operation/deployment.html).
- New transactor memory setting `memory-index-threshold`, and expanded documentation at [capacity](https://docs.datomic.com/operation/capacity.html).
- Overhauled the metrics reported by the transactor. Learn about the new metrics at [monitoring](https://docs.datomic.com/operation/monitoring.html).
- Fixed bug that caused incomplete garbage collection of storage on DynamoDB.

## 0.8.3767 2013/01/23

- Fixed bug where deleting a database could cause subsequent indexing jobs of other databases to fail with "No implementation of method::queue-database-index …"
- New `Alarm` metric is fired whenever the transactor encounters a problem that requires manual intervention.
- Improved indexing performance, particularly for larger strings and binary values.

## 0.8.3731 2013/01/09

- Improved resilience in the face of unreliable or throttled storage.
- Added write-concurrency setting to the transactor properties file, which can be used to limit the number of concurrent writes to storage.
- Added StorageBackoff metric, which records the amount of time spent backing off and waiting for storage.
- Fixed bug where fulltext queries would sometimes seek off the end of an index and throw IOException.

---

## 0.8.3705 - Breaking change to peer/transactor communication - 2013/01/09

Upgrade to HornetQ 2.2.21. This change breaks compatibility with older versions, so peers and transactors must be upgraded together.

## 0.8.3704 2013/01/03

Fixed bug in HA support. HA does not work with the three previous releases (3692, 3664, 3655). If you are running a standby transactor for HA, make sure to upgrade to this release.

## 0.8.3692 2012/12/28

- Fixed bug where restore would restore to a version other than the most recent in the backup storage.
- Improved backup CLI: You can now `list-backups` to see different points in time (t) that are backed up to a storage, and pass a `t` argument to `restore-db` to restore from a version other than the most recent in the backup storage.
- Fixed bug: all transaction errors now throw exceptions consistent with the API documentation. (Some errors had been thrown early, on transact, rather than on dereferencing the transaction future.)
- Fixed a bug where a portion of the history index was not considered by the `asOf` database filter.
- Fixed bug where, under unusual circumstances, datoms could reappear in the present index, even though they had been retracted.
- Performance optimization: Peer cache now reads ahead on indexes.
- Updated to spymemcached 2.8.9.
- Fixed bug where retraction of a nonexistent fulltext attribute value could cause subsequent indexing jobs to fail.
- Fixed a bug that could prevent connection to a database after a cycle of create / backup / delete / restore.

## 0.8.3664 2012/12/15

Added gap-detection validation when reading log on startup. Operating with a gap, while not resulting in loss of data, can cause violations of uniqueness and cardinality constraints. Users on releases prior to 0.8.3664 are strongly encouraged to move to 0.8.3664 or later as soon as possible.

## 0.8.3655 2012/12/12

- Fixed bug where entities could have more than one:db/fn attribute.
- Fixed bug in log loading where a window of data could be invisible on restart, even though that data is present in the log.

## 0.8.3646 2012/12/09

- Fixed bug where database indexing fails after a backup/restore cycle.
- Fixed bug in Clojure API where transaction futures sometimes would return (rather than throw) an exception.
- Fixed bug in Java API where transaction futures would occasionally return an object that is not the documented map.
- Better error reporting for some kinds of invalid queries.
- Alpha support for CORS in the REST service. Note that the bin/rest args have changed. See [rest](https://docs.datomic.com/reference/rest.html)\] for details.

## 0.8.3627 2012/11/27

- Fixed bug where the first restore of a database to a new storage did not include the most recent data, even though that data was present in the backup. (Subsequent restores were unaffected).
- Queries now take a:with clause, to specify variables to be kept in the aggregation set but not returned.
- Database.filter predicates now take two arguments: The unfiltered Database value and the Datom.
- You can now retrieve the Database that is the basis of an entity with Entity.db.
- You can now install a release of Datomic Pro into your local Maven repository with bin/maven-install.

## 0.8.3619 2012/11/21

- Alpha release of Database.filter, which returns a value of the database filtered to contain only the datoms satisfying a predicate.
- New AWS metric: IndexWrites.
- Peers no longer need to include a Datomic-specific Maven repository, as [Fressian](https://github.com/Datomic/fressian) is now available from Maven Central and clojars.

## 0.8.3611 2012/11/19

- Added memory-index-max setting to allow higher throughput for e.g. import jobs.
- Bugfix: fixed bug that prevents indexing jobs from completing with some usages of fulltext attributes.
- Added additional AWS instance types to AMI setup scripts.
- Bugfix: Cloudformation generation now respects aws-autoscaling-group-size setting.
- Fixed broken query example in GettingStarted.java.
- Fixed docstring for datomic.api/with.

## 0.8.3599 2012/11/09

- Fixed "No suitable driver" error with dev: and free: protocols in some versions of Tomcat.
- Updated bin/datomic `delete-cf-stack` command to work with multiregion AWS support.

## 0.8.3595 2012/11/04

- Fixed bug that prevented building queries from Java data.
- Transactor AMIs are now available in all AWS regions that support DynamoDB: us-east-1, us-west-1, us-west-2, eu-west-1, ap-northeast-1. and ap-southeast-1.
- Breaking change: CloudFormation properties file has a new required key `aws-region`, allowing you to select the AWS region where a transactor will run.

## 0.8.3591 2012/11/02

- Preliminary support for Couchbase and Riak storages.
- Breaking change: DynamoDB storage is now region-aware. URIs include the AWS region as a first component. The transactor properties file has a new mandatory property `aws-dynamodb-region`.
- Breaking change: CloudWatch monitoring is now region-aware. If using CloudWatch, you must set `aws-cloudwatch-region` in the transactor properties.
- Transactions now return a datomic.ListenableFuture, allowing a callback on transaction completion.
- Fixed bug in the command line entry point for restoring a database, which was defaulting to the most ancient backup instead of the most recent.
- Fixed bug that prevented restoring to a `dev:` or `free:` storage in some situations.

## 0.8.3561 2012/10/23

- Breaking change: db.with() now returns a map like the map returned from Connection.transact().
- Incompatible and unsupported schema changes now throw exceptions.
- Better error messages when calling a query with bad or missing inputs.
- Documented system properties.

## 0.8.3551 2012/10/10

Fixes to alpha aggregation functions.

## 0.8.3546 2012/10/09

Alpha support for aggregation functions in query.

## 0.8.3538 2012/09/21

- Fixed bug where variables bound by a query:in clause were not seen as bound inside rules.
- Added `invoke` API for invoking database functions.

## 0.8.3524 2012/09/16

Fixed bug that caused temporary ids to read incorrectly in transaction functions, causing transactions to fail.

## 0.8.3520 2012/09/14

- Fixed but that prevented the catalog page from loading on REST service when running against a persistent storage.
- Enhancements to REST documentation.

## 0.8.3511 2012/09/14

The REST service is now its documentation. Just point a browser at the root of the server:port on which you started the service. Note that the "web app" that results **is** the service. It is not an app built on the service, nor a set of documentation pages about the service. The URIs, query params, and POST data are the same ones you will use when accessing the service programmatically.

## 0.8.3488 2012/09/06

Initial version of REST service.

## 0.8.3479 2012/09/04

New API: `Entity.touch` touches all attributes of an entity, and any component entities recursively.

## 0.8.3470 2012/08/31

- Fixed bug where some recursive queries return incorrect results.
- Fixed bug in command-line entry point for restoring from S3.

## 0.8.3460 2012/08/29

- Fixed bug where peers could continue to interact with connections to deleted databases.
- Fixed query bug where constants in queries were not correctly joined with answers.
- Fixed directory structure in JAR file format, which was causing problems for some tools.
- Report serialization errors back to data function callers, rather than make them wait for transaction timeout.
- Better error messages for some common URI misspellings.

## 0.8.3438 2012/08/22

Fixed bug in:db/txInstant that prevented backdating before db was created.

## 0.8.3435 2012/08/21

- New API: `Peer.resolveTempid` provides the actual database ids corresponding to temporary ids submitted in a transaction.
- Changed API: you can now explicitly specify the:db/txInstant of a transaction. This facilitates backdating transactions during data imports.
- changed API: transaction futures now return a map with DB\_BEFORE, DB\_AFTER, TX\_DATA and TEMPIDS.
- Changed API: transaction report queues now report the same data as calls to `Connection.transact`
- Changed API: transaction report TX\_DATA now includes retractions in addition to assertions.
- New API: `Database.basisT` returns the t of the most recent transaction
- Bugfix: fixed bug that prevented AWS provisioning scripts from running

## 0.8.3423 2012/08/16

- New API: `database.history` returns a value of a database containing all assertions and retractions across time.
- New API: `database.isHistory` returns true if database is a history database.
- New API: `datom.added` returns true if datom is added, false if retracted.
- Changed API: the Datom relation in a query now exposes a fifth component, containing a boolean that is true for adds, false for retracts.
- Changed API: removed `Index` class, range capability now available directly from `Database`.
- Bugfix: calling `keys` on an entity with no current attributes no longer throws NPE.
- Updated to use the recent (12.0.1) version of Google Guava.
- When a peer calls `deleteDatabase`, shut down that peer's connection.

## 0.8.3397 2012/08/07

- Fixed bug where some recursive queries returned partial results.
- Simplified license key install: pro license keys are installed via a `license-key` entry in the transactor properties file.
- Connection catalog lookup is cached, so it is inexpensive to call `Peer.connect` as often as you like.
- Improved fulltext indexing and query performance.

## 0.8.3372 2012/07/31

- Changed API: query clauses are considered in order.
- Changed API: when navigating entities, references to entities that have a `db/ident` return that ident, instead of the entity.
- New API: `database.index` supports range requests.
- Fixed broken dependency that prevented datomic-pro peer jar from working with DynamoDB.
- New API: `peer.part` function returns the partition of an entity id.
- New API: `peer.toTx` and `Peer.toT` conversion functions.
- Entity equality is ref-like, i.e. identity-based.
- Eliminated resource leaks that affected multiple databases and some failover scenarios.

## 0.8.3343 2012/07/25

- Added API entry points for generating semi-sequential UUIDs, a.k.a squuids.
- Use correct maven artifact ids: `datomic-free` for Datomic Free Edition, and `datomic-pro` for Datomic Pro Edition.
- Fixed bug where queries could see retracted values of a cardinality-many attribute.