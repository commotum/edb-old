---
title: "Tx-Stats | Datomic"
source: "https://docs.datomic.com/reference/tx-stats.html"
description: "Learn how to use :tx-stats to map every Datomic transaction."
word_count: 552
---

## Tx-Stats

## Background

Accruing a set of datoms to transact to a database value can require, for each datom, resolving its entity ID, checking a unique value, and generating a composite tuple. Transactions can also express sets of datoms via transaction function shorthands, which must be expanded to primitive list forms. Each redundant datom (about existing entities) is eliminated, and novel datoms may generate a corresponding retraction. Together, these tasks can take time because they need to read information from the given database value.

## Problem

To design and maintain high performance systems, it is important to understand the fine-grained costs of applying each transaction. io-stats measures the index reads for transaction processing, i.e. the physical effects, but it is difficult to reason backwards from the effects to their semantic cause.

## Approach: Tx Stats

Datomic logs a:tx-stats map for every transaction. This map contains metrics describing the work performed to accrue a set of datoms, including counts of datoms and processing durations.

Tx-Stats does not include metrics about making a transaction durable, e.g. writing it to the log.

Everything about tx-stats is alpha and subject to change in future releases.

```
2024-06-01 05:16:29.440 INFO  default    datomic.transaction -
{:tid 178, :txid #uuid "665aae9f-b8b5-4eb2-a3a2-0871c520bdac",
 :datom-count 108,
 :apply-msec 1.63,
 :io-stats {:api :tx-with, :io-context :my-barrel/tx, :api-ms 1.61,
            :reads {:avet 80, :aevt 57, :eavt 3, :dir 96, :ocache 236}},
 :pid 43974,
 :event :tx/process,
 :msec 23.9,
 :t 33278227008,
 :tx-stats {:dedup-tx-ms 0.15,
            :tx-fn-ms 0.0,
            :dedup-pf-ms 0.3,
            :comp-tx-ms 0.0,
            :dup-datoms 16,
            :res-pf-ms 0.45,
            :ucheck-ct 2,
            :considered-datoms 127,
            :comp-ct 0,
            :ucheck-tx-ms 0.02,
            :ucheck-pf-ms 0.02,
            :dedup-ct 19,
            :comp-pf-ms 0.0,
            :res-tx-ms 0.34,
            :res-ct 26}}
```

In Cloud, tx-stats appear under the “TxStats” key of the “TxSucceeded” message, with all keys converted to CloudWatch JSON Pascal case conventions, e.g.:

```
{
  "Msg": "TxSucceeded",
  "DbId": "7e29bec5-a1fe-4f8f-b46e-60ebd30acf1f",
  "T": 1007523406,
  "IoStats": {
    "IoContext": "MyAppTransaction",
    "Api": "TxWith",
    "ApiMs": 3.75,
    "Reads": {
      "Avet": 10,
      "InflightLookupMs": 2.02,
      "Aevt": 3,
      "Ocache": 13
    }
  },
  "TxStats": {
    "DedupTxMs": 0.14,
    "TxFnMs": 0.0,
    "DedupPfMs": 0.37,
    "CompTxMs": 0.0,
    "DupDatoms": 2,
    "ResPfMs": 4.24,
    "UcheckCt": 2,
    "ConsideredDatoms": 65,
    "CompCt": 0,
    "UcheckTxMs": 0.02,
    "UcheckPfMs": 0.04,
    "DedupCt": 8,
    "CompPfMs": 0.0,
    "ResTxMs": 2.32,
    "ResCt": 17
  },
  "Type": "Event",
  "Tid": 559,
  "Timestamp": 1717466409767
}
```

## The Tx-Stats Map

The tx-stats map can have at least the following keys:

| Key | Semantic | Unit |
| --- | --- | --- |
| :res-ct | number of datoms considered having:db.unique/identity attributes | datoms |
| :res-pf-ms | sum of time spent prefetching the resolution of unique identities | millis |
| :res-tx-ms | sum of time spent on transaction thread resolving unique identities | millis |
| :comp-ct | number of datoms with composite tuple attrs generated | datoms |
| :comp-pf-ms | sum of time spent prefetching the constituents of composite tuples | millis |
| :comp-tx-ms | sum of time spent on transaction thread looking up the constituents of composite tuples | millis |
| :dedup-ct | number of datoms that needed to be checked for redundancy | datoms |
| :dedup-pf-ms | sum of time spent prefetching redundancy checks | millis |
| :dedup-tx-ms | sum of time spent on transaction thread checking redundancy | millis |
| :ucheck-ct | number of datoms with whose values need to be checked for uniqueness | datoms |
| :ucheck-pf-ms | sum of time spent prefetching uniqueness checks | millis |
| :ucheck-tx-ms | sum of time spent on transaction thread checking uniqueness | millis |
| :tx-fn-ms | sum of time spent expanding transaction fns | millis |
| :dup-datoms | number of datoms redundant with the db-before | datoms |
| :considered-datoms | number of datoms considered in transaction | datoms |

The tx-stats map is open to extension at any time, and you may see additional keys not documented here.