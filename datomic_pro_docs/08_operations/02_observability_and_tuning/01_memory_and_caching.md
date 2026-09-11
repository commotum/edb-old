---
title: "Memory and Caching"
site: "Datomic"
source: "https://docs.datomic.com/operation/caching.html"
description: "Discover how to cache data in Datomic."
word_count: 962
---

Datomic provides a number of caches, all of which are transparent to application code:

Datomic caches only immutable data, so all caches are valid forever.

## Memory Index

Recent datoms are cached in the memory index on the transactor and on all peers. The memory index size is [controlled](https://docs.datomic.com/operation/capacity.html#memory-index-defaults) by the *memory-index-threshold* and *memory-index-max* [settings on the transactor](https://docs.datomic.com/operation/system-properties.html#transactor-properties). The actual size of the memory index will rarely rise much above *memory-index-threshold*, except during data imports when it may reach *memory-index-max*.

Setting memory index configuration on a peer is meaningless; peers must be able to handle the memory index given to them by the transactor.

The memory index is managed as follows:

### On the Transactor

- The transactor builds the memory index from the log before it begins serving the database.
- The transactor updates the memory index after it writes a transaction to the log.
- When an indexing job is completed, the transactor drops the portion of the memory index that is no longer needed, as it is now in the durable index.

### On the Peer

- A peer builds the memory index from the log before the call to connect returns.
- A peer updates the memory index when notified by the transactor of a transaction.
- A peer drops the portion of the memory index that is no longer needed when notified by the transactor that an index job has been completed.

## Entity Caching

Data retrieved via get (and touch) on the [Entity API](https://docs.datomic.com/reference/entities.html) is cached for the lifetime of the *Entity* instance.

## Object Cache

Datomic processes maintain an LRU Java object cache that contains segments of [index](https://docs.datomic.com/query/indexes.html) or [log](https://docs.datomic.com/api/log.html), typically a few thousand datoms per segment. When a Datomic process needs data, Datomic looks in the object cache first. If the data is unavailable locally, Datomic reads the data from Memcached or storage and then stores the data in the cache.

The object cache size is limited by the *object-cache-max* property on the transactor and by the *datomic.ObjectCacheMax* Java system property on the peer.

Unlike the memory index, the object cache can vary (in both configuration and contents) across the different processes in a Datomic system. Because each process maintains its own cache, it will automatically adjust over time to its workload.

## Memcached

Memcached is an optional addition to a Datomic system, providing another level of cache between the in-process object cache, Valcache (if configured), and storage. Adding Memcached to a system can reduce the load on storage, and can reduce the latency associated with populating the object cache.

Memcached configuration is [controlled](https://docs.datomic.com/accessing/integrating-peer-lib.html#configuring-caching) by the *memcached* [property on the transactor](https://docs.datomic.com/operation/system-properties.html#transactor-properties) and the *datomic.memcachedServers* [Java system property on the peer](https://docs.datomic.com/operation/system-properties.html#peer-properties).

You can set up more than one Memcached, and each Datomic process can choose via configuration which one(s) it uses. Datomic uses only UUIDs for Memcached keys, so Datomic can coexist with other uses of a Memcached installation. Additionally, if you specify more than one Memcached server, Datomic will distribute values across all instances, effectively enabling a single cache the size of the sum of all the instances.

When configured to use Memcached, a Datomic process will automatically write into Memcached any segment it needs that is not already present in Memcached. In addition, the transactor (only) will write index and log segments to Memcached as the segments are produced.

The *datomic.memcachedServers* property specifies a fixed list of Memcached servers. To change the list of servers, you must change the value of this property and restart the affected peer and transactor processes. If you are using Amazon ElastiCache, a better approach is to use [automatic discovery](#node-auto-discovery).

### SASL Auth

Datomic supports SASL authentication to a Memcached cluster. If *datomic.memcachedUsername* and *datomic.memcachedPassword* (memcached-username and memcached-password in transactor properties) are set, Datomic will use those credentials to authenticate its connection to Memcached.

### CloudWatch metrics

If you are using Cloudwatch to [monitor transactors](https://docs.datomic.com/operation/monitoring.html), Datomic records the following Memcached metrics:

- *Memcache* is the number of read requests to Memcached
- When reading from storage, *ReaderMemcachedPutMusec* is the latency for successful writes to Memcached and *ReaderMemcachedPutFailedMusec* is the latency for failed writes
- When writing to storage, *WriterMemcachedPutMusec* is the latency for successful writes to Memcached and *WriterMemcachedPutFailedMuSec* is the latency for failed writes

### Automatic Node Discovery

If you are using Amazon ElastiCache, you can take advantage of [automatic discovery](https://docs.aws.amazon.com/AmazonElastiCache/latest/mem-ug/AutoDiscovery.html) of cache nodes. Set the *memcached* property (in transactors) or *datomic.memcachedServers* property (in peers) to the [Memcached cluster's configuration endpoint](https://docs.aws.amazon.com/AmazonElastiCache/latest/mem-ug/Endpoints.html) and enable automatic discovery by setting the *datomic.memcachedAutoDiscovery* parameter to *true*.

When automatic discovery is enabled, any changes made to the cluster (such as adding or removing a node) are going to be noticed by Datomic without the need for configuration changes or any kind of intervention. The system will automatically take advantage of new nodes and stop using removed nodes as soon as the changes are observed.

Automatic discovery of Memcached nodes requires Datomic 1.0.6316 or later.

## Usage Notes

- All forms of Datomic caching are transparent to application code.
- Datomic caching requires no configuration other than the overall size of the object cache. The [capacity planning](https://docs.datomic.com/operation/capacity.html) docs guide setting cache sizes.
- The transactor benefits from both the object cache and Memcached in the course of ordinary transaction processing. For example, the transactor uses database indexes for both cardinality and uniqueness checks.
- Because the object cache lives in your application's address space, it competes with your application for available memory. Take this into account when planning your object cache and VM RAM settings.
- The independent configuration of the object cache and Memcached supports task-specific cache strategies. For example, a production system could have two groups of peers with different Memcached clusters, one for OLTP and one for OLAP loads. Meanwhile, a support engineer's machine could connect to the same system using its own local Memcached install.

---

> ATOMIC-NOTE — development trace: [Immutable cached data and mutable cache policy](01_memory_and_caching.atomic.md) links the ownership, sizing, memory-index and optional-integration passages above to recovered source and current Rust owners. It explains the selected recency adaptation and cache-owned byte versus process-memory limits. This added commentary is separate from the reference text and records no new test execution.
