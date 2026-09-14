---
title: "Io-Stats | Datomic"
source: "https://docs.datomic.com/reference/io-stats.html"
description: "Explore Datomic io-stats to monitor and optimize database performance. Learn how to access and interpret these metrics."
word_count: 1813
---

## Io-Stats

This reference describes io-stats and includes:

Everything about io-stats is alpha and subject to change in future releases.

## Background

Any application or task has a [working set](https://en.wikipedia.org/wiki/Working_set) of data. Where that data is located/cached is an important part of architecting a system; a well-architected system has to plan for moving through tiers of cache. Otherwise, applications will encounter unexpected and dramatically different performance when data accesses spill over to the next tier. In Datomic, this can happen when there is too much reliance on increasing the first tier [object cache](https://docs.datomic.com/operation/architecture.html#object-cache) (an in-memory cache of Java objects) to handle the entire working set.

## Problem

To plan for moving through tiers of cache, developers need to understand the capacity and latency of each tier. Then for a given API call, they need to know where (in which tier) data is coming from, what part of the application called the API, and why the API needs that particular data.

In Datomic Pro, the transactor uses [indexes](https://docs.datomic.com/query/raw-index-access.html) to enforce invariants, e.g. AVET for uniqueness checks, and AEVT for cardinality and redundancy elimination. The query engine chooses a suitable index sort for each relational clause. These operations nest – both transactions and queries can call other queries and so on.

Before io-stats, Datomic APIs did not provide direct access to I/O information, forcing developers to reason backward from downstream effects, e.g. inferring that a query is spilling into the next cache tier when it performs very differently from other superficially similar queries.

## Approach: Io-Stats

Io-stats summarizes the input and output (I/O) associated with a Datomic API call. Io-stats allows you to analyze where data is coming from, providing counts and latencies for index segments accessed from each tier. Io-stats shows what API call was made, and supports a user-provided io-context to distinguish different operations. And finally, io-stats provides some help with why, telling you the count of each different index sort order (:aevt/:avet/:eavt/:vaet).

Io-stats are reported as a Clojure map, [described in detail below](#map). Io-stats are logged automatically for transactions, and you can explicitly request io-stats be returned from various transaction and query APIs.

## Transaction Logging

Datomic logs an io-stats map for every transaction. In Datomic Pro, io-stats are logged under the:tx/process:event, as shown below:

```
2022-03-29 23:16:26.567 INFO  default    datomic.transaction - {:tid 115,
:txid #uuid "6243cb8a-2b3c-4031-a4cd-8c7df49e3981",
:datom-count 1, :apply-msec 4.71, :pid 76957,
:io-stats {:api :tx, :api-ms 5 :reads {:ocache 2}},
:event :tx/process, :msec 16.0, :t 1008}
```

In Cloud, io-stats are logged under the “TxSucceeded” message, with all keys converted to CloudWatch JSON Pascal case conventions, e.g.:

```
{
    "Msg": "TxSucceeded",
    "DbId": "5644c1a8-aedc-44c6-9e70-1b9a3b035326",
    "T": 6,
    "IoStats": {
    "IoContext": "DbTx",
    "Api": "TxWith",
    "ApiMs": 4.86,
    "Reads": {
        "Avet": 2,
        "Aevt": 3,
        "ValcacheDirectMs": 0.14,
        "Ocache": 5,
        "ValcacheDirect": 2
    }
    },
    "Type": "Event",
    "Tid": 249,
    "Timestamp": 1665667355838
}
```

You can search for io-stats in the Cloudwatch console with e.g. {$.Msg=”TxSucceeded”}.

Note that transaction logging correlates io-stats with transaction t. Given t, you can use the [log API](https://docs.datomic.com/reference/log.html) to query the datoms produced by the transaction.

## Transaction API Io-Stats

You can request that io-stats be included in the map returned from transact by adding `:io-context` as a trailing arg to transact:

```
(d/transact conn {:tx-data [...] :io-context :tx/example})
```

```
=> ;; tx result will include :io-stats key
```

## Query API Io-Stats

The query, qseq, pull, and pull-many APIs all include arities that allow you to pass `:io-context`, a qualified keyword that you can use to distinguish different domain operations. When you pass `:io-context`, Datomic will return a map with `:ret` (the query result) and `:io-stats` (an io-stats map).

For example, the following query includes `:io-context`:

```
(d/q {:query '[:find (count ?a)
                   :in $ ?aname
                   :where [?a :artist/name ?aname]]
          :args [db "The Beatles"]
          :io-context :artist/by-name})
```

```
{:io-stats {:io-context :artist/by-name,
            :api :query,
            :api-ms 105.08,
            :reads {:dir 1,
                    :avet 2,
                    :valcache-direct 2,
                    :efs 2,
                    :efs-ms 26.27,
                    :valcache 2,
                    :dir-load 1,
                    :valcache-ms 50.57,
                    :avet-load 2,
                    :ocache 2}},
 :ret [[1]]}
```

## Nested Io-Stats

The io-stats for a top-level API include the total I/O for all nested calls. Often, you will also want to know the io-stats breakdown for nested calls, if any. For example, when tuning the performance of a transaction function that issues two queries, you could focus your efforts if you knew from nested io-stats that one of the queries dominates overall performance.

When nested API calls have an `:io-context`, the io-stats for nested calls are aggregated by `:io-context` under the:nested key. For example, imagine that a transaction with `:io-context` of `:transfer/funds` calls a query with `:io-context` of `:transfer/check-balance` twice and a query with `:transfer/check-auth` once. The transaction returns (and logs) an io-stats map with the following shape:

```
{:io-context :transfer/funds
 :reads ;; totals for entire tx
 :nested
 {:transfer/check-balance
  ;; io-stats map that sums across 2 calls to check-balance
  :transfer/check-auth
  ;; io-stats map of the one call to check-auth
  }}
```

## The Io-Stats Map

The io-stats map can have at least the following keys at the top level:

| Key | Value | Example |
| --- | --- | --- |
| :api | Datomic API name, as keyword | :query/:qseq/:tx-with/:pull/:pull-many |
| :api-ms | Datomic API time in milliseconds (wall clock time from start of API call until completion) | 11.42 |
| :reads | Map breakdown of segment reads, see next section | {:ocache 211,:valcache 2} |
| :nested | A map from:io-context keyword to a nested io-stats map covering the sum of all nested calls with that:io-context | {:transfer/check-balance, io-stats-map,:transfer/check-auth, io-stats-map} |
| :io-context | (Optional) qualified keyword passed to Datomic APIs by the caller, used to identify different domain operations | :transfer/funds |

The io-stats map and its submaps are open to extension at any time, and you may see additional keys not documented here.

### Io-Stats:reads Map

The `:reads` map reports the following information:

- Counts of segments accessed through each cache/storage tier, and the total time spent waiting on I/O in each storage tier
- Counts of segments broken down by index sort, and by whether the segment had to be loaded

Datomic accesses cache and storage tiers in the following order, from `:ocache` down to storage of record:

| :reads Map tier keys | Value |
| --- | --- |
| :ocache | Count of segment accesses via the in-process object cache, a cache of Java objects |
| :valcache | Count of segments accessed through Valcache – an SSD cache |
| :memcached | Count of segments accessed through Memcached |
| (storage of record):ddb/:sql/:cass etc. | Count of segments accessed through Datomic storage (keys match the [storage protocol](https://docs.datomic.com/overview/storage.html#storage-services) in use) |
| :inflight-lookup-ms | Sum of time spent waiting on an concurrent request for the same segment |

There are several important things to know when interpreting the tier keys in the `:reads` map.

- The first (object cache) tier is special. Segments in the object cache are *already in memory* and do not require I/O.
- The counts for higher tiers include the misses that read through to the next tier. So the `:ocache` count will always be greater than the count of the next tier, and so on.
- The cache tiers between the object cache and storage are optional. If your Datomic system does not have one of these tiers deployed, then the keys will be absent.
- For the tiers that require I/O, there is a corresponding {tier}-ms key that is the sum of milliseconds threads spent waiting on reads from that tier. Note that due to parallelism, a {tier}-ms value may be greater than the overall `:api-ms`!

The `:reads` map also reports which index sorts were used, and how many times each sort had to be loaded into memory:

| :aevt/:vaet/:eavt/:avet | Count of accesses to different [index sorts](https://docs.datomic.com/query/raw-index-access.html) |
| --- | --- |
| :{index-sort}-load | Count per index sort of segments that had to be loaded into memory, i.e. were not already present in the ocache. |

## Interpreting Io-Stats

To get the most benefit from io-stats, you should look at them under realistic load, either from production logs or from simulated load in a test environment. Realistic load is important because io-stats depend not just on the API and database but also on the current state of the caches and indexes, as described below.

### Io-Stats Depend on the Current State of the Caches

Datomic dynamically updates caches as data is used, bringing frequently needed segments into closer, lower latency cache tiers. The most trivial example of this is that running the same query twice on an unloaded system can produce very different io-stats. Observe the difference in two back-to-back calls finding the average artist name length in [Mbrainz](https://github.com/Datomic/mbrainz-sample):

```
(def avg-name-q '[:find (avg ?aname-len)
                  :with ?a
                  :where [?a :artist/name ?aname]
                  [(count ?aname) ?aname-len]])
(d/query {:query avg-name-q
          :args [db]
          :io-context :artist/avg-name-length})
```

```
;; first call
=> {:ret [[13.08504033265786]],
    :io-stats {:api :query, :api-ms 1096.2,
               :io-context :artist/avg-name-length
               :reads {:aevt 607, :aevt-load 414, :ocache 607,
                       :dev 212, :dev-ms 304.1}}}
;; second call
=> {:ret [[13.08504033265786]],
    :io-stats {:api :query, :api-ms 938.9,
               :io-context :artist/avg-name-length
               :reads {:aevt 405, :ocache 405}}}
```

The first call begins with no data in the object cache, so you would expect to see reads down into other tiers – in this case down to dev storage. On the other hand, the second call shows no reads-through to dev, and is satisfied entirely from the object cache! From this, we can infer that the segments needed by the query fit entirely in the object cache. Real-world queries are likely to land in between these two extremes, finding some but not all the data they need in each cache tier.

### Io-Stats Depend on the Current State of the Indexes

Datomic keeps a window of the most recent transactions in memory at all times. Queries against this recent data will see artificially low io-stats. This makes little difference in real-world databases but can dominate results in tiny databases created for experiments. Data used for performance testing should ideally be similar in size to production data.

## Relating Io-Stats to Transaction Time

The tx-with time reported by io-stats is only one component of the total transaction time experienced by a caller, which is the sum of:

- Interprocess communication time if requesting and transacting processes are not the same (e.g. Datomic Pro peers and transactors)
- Time queued on the transacting process
- The io-stats tx-with time (applying the transaction, enforcing invariants)
- Storage write time

To measure total transaction time, time calls to transact from Datomic [peers or clients](https://docs.datomic.com/datomic-overview.html#datomic-APIs).

The transactor time includes everything that can be measured from the transacting process, i.e. everything except interprocess communication. The Datomic Pro transactor logs this time under the `:msec` key of the `:tx/process` event.

The tx-with time reported by transaction io-stats isolates the time actually spent processing the transaction. Note that if you improve the tx-with time for a transaction, you also improve the transactor time for any other concurrent transactions enqueued behind it.

The different measures of transaction time are summarized in the table below:

|  | How to measure | Includes network time | Includes time queued on transactor | Includes tx-with time | Includes time waiting on storage write |
| --- | --- | --- | --- | --- | --- |
| **Total transaction time** | Time peer call to transact | Y | Y | Y | Y |
| **Transactor time** | Transactor log:tx/process:msec key | N | Y | Y | Y |
| **Tx-with time** | Transactor log io-stats:api-ms | N | N | Y | N |