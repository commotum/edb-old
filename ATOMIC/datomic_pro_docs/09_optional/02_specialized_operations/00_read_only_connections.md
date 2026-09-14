---
title: "Read Only Connections"
site: "Datomic"
source: "https://docs.datomic.com/operation/read-only.html"
description: "Read Only Connections"
word_count: 639
---

Some tasks need only a database value, with none of the infrastructure, semantics, cost, or contention of an online system. For such situations, Datomic now provides read-only connections, which retrieve a single database value directly from storage. No transactor required.

Read-only connections extend the immutable database values central to Datomic's design, making them directly accessible for validating backups, performing analytical work in a cheaper region, inspecting a database value during an incident, and more.

### Objective

Users need to access database values for a wide range of tasks that have nothing to do with writing or coordinating changes:

- Validate that a backup contains the expected data
- Run analytical or ETL workloads against a past database value
- Inspect a database value during an incident without touching production
- Run performance tests against a real dataset while being able to configure peer resources (cache stack) to match production.

What these use cases share is they need a database value, not an online system.

### Obstacles

Before read-only connections, all of these tasks required a running transactor. Transactors cost money to run and operate, must be explicitly launched, and must be kept alive for the duration of the work even if no transactions are needed. Datomic backups are copies of databases, but they must be restored to live storage before they can be accessed. That means running a restore, waiting for the restore, launching a transactor to read and then tearing it all down. Peers receive novelty broadcasts from the transactor regardless of whether they need them. Read-only workloads pay coordination costs they don't need.

### Approach

Starting with Datomic `1.0.7622`, `datomic.api/connect` supports read-only connections to any database URI, including backups, without requiring a transactor.

- Add a `read-only=true` query param to any existing Datomic URI to get a read-only connection to storage, sans transactor.
- Or use the new `datomic:backup:` URI protocol to connect directly to a backup, querying its data without first restoring.
- Read-only connections honor any configured cache stack (valcache, memcache), giving analytical workloads their own cache without evicting data from online peers.
- `d/list-backups` is now a first-class peer API, equivalent to the `bin/datomic list-backups` shell command and providing connectable URIs for programmatic use. See: [list-backups](https://docs.datomic.com/clojure/index.html#datomic.api/list-backups)

## URI Syntax

Add the `read-only=true` query param to any existing [connection URI](https://docs.datomic.com/clojure/index.html#datomic.api/connect):

```
datomic:ddb://{aws-region}/{dynamodb-table}/{db-name}?read-only=true

; Note that sql storage has an additional new syntax where the nested JDBC URI follows a #
datomic:sql://{db-name}?read-only=true#{jdbc-uri}

; For URIs that already carry query parameters, append with ~&~:
datomic:dev://{host}:{port}/{db-name}?password={password}&read-only=true
```

> Note: Because its storage services are embedded in the transactor, a read-only connection to `dev:` still requires a running transactor.

### Backup URIs (datomic:backup:)

The `datomic:backup:` protocol accepts any backup location previously used with the [`bin/datomic backup-db`](https://docs.datomic.com/operation/backup.html) tool. It returns the latest backup unless given a t query param

```
;; Latest t in a local backup
datomic:backup:file:/path/to/backup

;; A specific backup t
datomic:backup:file:/path/to/backup?t=12345

;; S3 backup (requires software.amazon.awssdk/s3 on the classpath)
"datomic:backup:s3://my-bucket/path/to/backup-dir"
```

Call [`d/list-backups`](https://docs.datomic.com/clojure/index.html#datomic.api/list-backups) to discover the snapshots available in a backup location and find valid `t` values:

```
(d/list-backups "file:///full/path/to/backup-dir")
;; => {:backups [{:t 12345, :connect-uri "datomic:backup:file:///full/path/to/backup-dir?t=12345"}
;;               {:t 11000, :connect-uri "datomic:backup:file:///full/path/to/backup-dir?t=11000"}]}
```

See also: [Backup and Restore](https://docs.datomic.com/operation/backup.html)

## Liveness

Read-only connections are not live. Each call to `d/db` or `d/log` returns the same value — the state of the database at the time the connection was established.

## Capabilities

### What read-only connections support

- `d/db`, `d/log` — which always return the same db or log
- `d/release` to release resources
- uses any enabled cache stacks (e.g. memcache, valcache)

## Examples

### Connecting to a database without a transactor

```
(def conn (d/connect "datomic:ddb://us-east-1/my-table/my-db?read-only=true"))

(def db (d/db conn))
(d/q '[:find ?e :where [?e :package/delivered-at]] db)
```

### Connecting and querying from a backup

```
;; List available backup ~t~'s
(d/list-backups "file:///path/to/backup-dir")
;; => {:backups [{:t 148352, :connect-uri "datomic:backup:file:///path/to/backup-dir?t=148352"} ...]}

;; Connect to a specific backup (omit t for latest)
(def conn (d/connect "datomic:backup:file:///path/to/backup-dir?t=148352"))
(def db (d/db conn))
(d/q '[:find ?e :where [?e :package/delivered-at]] db)
```