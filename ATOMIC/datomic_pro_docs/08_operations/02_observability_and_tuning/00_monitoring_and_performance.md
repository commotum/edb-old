---
title: "Monitoring and Performance"
site: "Datomic"
source: "https://docs.datomic.com/operation/monitoring.html"
description: "Explore Datomic's monitoring and performance capabilities with guidance on setup, configuration, and best practices."
word_count: 1698
---

This page covers:

Metric names are identified in the text by surrounding square brackets, e.g. *\[TransactionMsec\]*. Each reported value of a metric can include up to five different statistics: *samples*, *average*, *minimum*, *maximum*, and *sum*.

![monitoring.svg](_images/00_monitoring_and_performance/monitoring.svg)

## Tuning Tips

### Give your peers and transactor enough memory

You can set the maximum memory available to a JVM process with the *\-Xmx* flag to java (or to bin/transactor). The more memory you give a process, the more database indexes it will be able to cache locally in memory, avoiding trips to Memcached or storage.

### Use Memcached

Memcached is a [great fit for Datomic](https://blog.datomic.com/2012/07/memcache.html). Read more about configuring Memcached in the [caching documentation](https://docs.datomic.com/operation/caching.html).

### Give memcached enough memory

Memcached can deliver data to peers with very low latency (on the order of 1 millisecond for a segment). If you can fit your entire database (or even the commonly-used working set) in Memcached, your read and query performance will be quite good.

Diagnostic: if your *\[Memcache\]* average is substantially less than 1.0, then you can serve more reads from Memcache by increasing the size of your memcached cluster.

### Don't Benchmark Against the Memory Database

The memory database is a convenience for development, testing, and temp data. Memory is not good for storage-sized datasets, as memory isn't storage-sized.

### Provision Enough DynamoDB Capacity

Check the DynamoDB section of [capacity planning](https://docs.datomic.com/operation/capacity.html).

### Run Large Imports on Cheap Storage, Then Backup/Restore

[Restoring](https://docs.datomic.com/operation/backup.html) a database consumes significantly less write capacity than building the database via transactions. So if your target system has metered storage pricing (e.g. DynamoDB), it may make sense to do large import jobs on less expensive storage, backup, and then restore to the target storage.

### Use Metrics to Analyze Write Activity

The speed with which a Datomic transactor can write data is measured with the *\[TransactionMsec\]* metric. The minimum, average, and maximum values of this metric, combined with the size of your transactions, characterize the write latency of the system from the transactor's perspective. (To measure write latency from a peer or client's perspective, instrument your code to measure the time from when [transact](https://docs.datomic.com/clojure/index.html#datomic.api/transact) is called until the transaction future is realized).

The *\[TransactionBytes\]* samples metric, again combined with the size of your transactions characterize the write throughput of the system. Note that to achieve maximum throughput you will need to pipeline transactions, i.e. arrange to have multiple transactions in flight at the same time.

## Custom Monitoring

You can integrate custom monitoring in both transactors and peers by registering a callback. The callback must be on the JVM process's classpath, so you will need to modify your deployment to e.g. include any necessary JARs and resources in the transactor's *lib* directory of the standard transactor.

Callbacks are registered via the *datomic.metricsCallback* Java system property in the peer or transactor, or via the *metrics-callback* setting in the transactor properties file.

### Register Callback on the Transactor

Register the callback for the transactor in the properties file:

```
metrics-callback=my.ns/handler
```

### Register callback on the peer

Register the callback for the peer with the command line argument:

```
-Ddatomic.metricsCallback=my.ns/handler
```

### Java callbacks

Java callbacks must be static public methods, taking a single *Object* argument, named as my.pkg.CallbackClass.handler, e.g.

```
metrics-callback=my.pkg.CallbackClass.handler
```

### Clojure Callbacks

Clojure callbacks must be functions that can take a single argument, named as my.ns/handler, e.g.

```
metrics-callback=my.ns/handler
```

### Callback Argument Format

The callback argument format is alpha and is subject to change. Currently, the argument is a map with keyword keys and either number or map values. If the value is itself a map, it will contain *:lo*, *:hi*, *:sum*, and *:count* components, all with numeric values.

The example below shows `:HeartbeatMsec` with a map value, and `:AvailableMB` with a numeric value:

```
{:HeartbeatMsec {:lo 5000, :hi 5001, :sum 55009, :count 11}, :AvailableMB 921.0}
```

The metrics are the same as those reported to CloudWatch. Note that the set of metrics is dynamic, open, and subject to change over time. A correct callback implementation should be tolerant of:

- Metric names it has never seen before
- Metric values in either of the shapes documented above, regardless of what shapes it has seen previously

## Configuring CloudWatch Monitoring

The Datomic Pro transactor can be configured to write metrics to [CloudWatch](https://aws.amazon.com/cloudwatch/). Find instructions for setting up CloudWatch [here](https://docs.datomic.com/operation/aws.html#s3-log-storage).

## Use CloudWatch From Anywhere

Note that you can use CloudWatch with any Datomic Pro transactor, regardless of which storage you are using, and regardless of where your transactor is located. Instructions for configuring CloudWatch for storages other than DynamoDB are [here](https://docs.datomic.com/operation/aws.html#other-storages).

## Available CloudWatch Metrics

### Transactor Metrics

The Datomic Pro transactor can create at least the following CloudWatch metrics, and may create others as well:

| Metric | Units | Statistic | Description |
| --- | --- | --- | --- |
| Alarm | Count | Sum | A problem has occurred (see alarms table below) |
| AvailableMB | MB | Minimum | Unused RAM on transactor |
| ClusterCreateFS | Msec | Maximum | Time to create a "file system" in the storage |
| CreateEntireIndexMsec | Msec | Maximum | Time to create an index, reported at end of indexing job |
| CreateFulltextIndexMsec | Msec | Maximum | Time to create fulltext portion of index |
| Datoms | Count | Maximum | Number of unique datoms in the index |
| DBAddFulltextMsec | Msec | Sum | Time per transaction, to add fulltext |
| FulltextSegments | Count | Sum | Total number of fulltext segments in the index, per index job |
| GarbageSegments | Count | Sum | Total number of garbage segments created |
| GcPauseMsec | Msec | Min, avg, max | Duration of garbage collector-induced pauses for G1 and ZGC |
| HeartbeatMsec | Msec | Avg | Time spacing of heartbeats written by active transactor |
| HeartbeatMsec | Msec | Samples | Number of heartbeats written by active transactor |
| HeartbeatMsec | Msec | Maximum | Longest spacing between heartbeats written by active transactor |
| HeartMonitorMsec | Msec | Min, avg, max | Time spacing of heartbeats observed by standby transactor |
| IndexDatoms | Count | Maximum | Number of datoms stored by the index, all sorts |
| IndexSegments | Count | Maximum | Total number of segments in the index |
| IndexWrites | Count | Sum | Number of segments written by indexing job, reported at end |
| IndexWriteMsec | Msec | Sum | Time per index segment write |
| LogIngestBytes | Bytes | Maximum | In-memory size of log when a database size |
| LogIngestMsec | Msec | Maximum | Time to play log back when a database starts |
| LogWriteMsec | Msec | Sum | The time to write to log per transaction batch |
| Memcache | Count | Avg | Memcache hit ratio, from 0 (no hits) to 1 (all hits) |
| Memcache | Count | Sum | Total number of cache hits for Memcache |
| Memcache | Count | Samples | Total number of requests to Memcache |
| MemoryIndexMB | MB | Maximum | MB of RAM consumed by memory index |
| MetricReport | Count | Sum | Count of successful metrics report writes over a 1 min period |
| ObjectCache | Count | Avg | Object cache hit ratio, from 0 (no hits) to 1 (all hits) |
| ObjectCache | Count | Sum | Total number of requests to object cache |
| MemcachedPutMsec | Msec | Min, avg, max | Distribution of successful memcache put latencies |
| MemcachedPutFailedMsec | Msec | Min, avg, max | Distribution of failed memcache put latencies |
| RemotePeers | Count | Maximum | Number of remote peers connected |
| StorageBackoff | Msec | Maximum | Worst case time spent in backoff/retry around calls to storage |
| StorageBackoff | Msec | Samples | Number of times a storage operation was retried |
| Storage{Get,Put}Bytes | Bytes | Sum | Throughput of storage operations |
| Storage{Get,Put}Bytes | Bytes | Samples | Count of storage operations |
| Storage{Get,Put}Msec | Msec | Min, avg, max | Distribution of storage latencies |
| TransactionBatch | Count | Sum | Transactions per batch written to storage |
| TransactionBytes | Bytes | Sum | Volume of transaction data to log, peers |
| TransactionBytes | Bytes | Samples | Number of transactions |
| TransactionBytes | Bytes | Min, avg, max | Distribution of transaction sizes |
| TransactionDatoms | Count | Sum | Datoms per transaction |
| TransactionApplyMsec | Msec | Min, avg, max | Time for the transactor to apply the transaction data to its in-memory representation of the db |
| TransactionMsec | Msec | Min, avg, max | Transaction time, includes the time to write to durable storage |
| Valcache | Count | Avg | Valcache hit ratio, from 0 (no hits) to 1 (all hits) |
| Valcache | Count | Sum | Total number of cache hits for Valcache |
| Valcache | Count | Samples | Total number of requests to Valcache |
| ValcachePutMsec | Msec | Min, avg, max | Distribution of successful Valcache put latencies |
| ValcachePutFailedMsec | Msec | Min, avg, max | Distribution of failed Valcache put latencies |
| WriterMemcachedPut | Count | Samples | Count of Memcached operations |

### Metrics Outside of AWS

If you're analyzing metrics through your own custom callback, or manually calculating statistics from the log, you may need to take additional steps to interpret metrics. The most common case is producing your average by dividing a *sum* value for a metric by its *count* value.

### Example Using Memcache

If you're looking at *memcache* metrics outside of AWS, you would interpret it as follows:

- *count* is the number of requests to Memcached
- *sum* is the total number of hits
- You would manually calculate misses as *count* minus *sum*
- You would manually calculate your hit rate as *sum* divided by *count*, producing, e.g., a value like 0.95

## Alarms

Cloudwatch allows you to create alarms when a Cloudwatch metric's value is outside of a predetermined range for a defined time period. Alarms can then send a notification to a pager or email, or trigger an Auto Scaling action to expand or contract some resource.

Datomic transactors have a **metric** named *\[Alarm\]*. This metric indicates that something has gone wrong with a transactor, and that human intervention is likely to be necessary. When deploying Datomic in production, you should create a Cloudwatch alarm that notifies you if the Datomic *\[Alarm\]* count is ever nonzero.

Each Datomic *\[Alarm\]* is accompanied by a more specific metric whose name is prefixed with *\[Alarm\]*. You can use these more specific alarms to plan your response to the problem, as shown in the table below.

| Alarm | Recommended action |
| --- | --- |
| AlarmIndexingJobFailed | Check for storage problems |
| AlarmBackPressure | Let import finish, increase [capacity](https://docs.datomic.com/operation/capacity.html) |
| AlarmUnhandledException | Contact Datomic support |
| Alarm{AnythingElse} | Contact Datomic support |

## Internal Monitoring

Metrics and events not documented above are *internal* to Datomic. Internal monitoring is implementation-specific and subject to change at any time. Do not build systems that rely on internal monitoring.

The descriptions of internal metrics and events below are provided for informational purposes only.

*AlarmStorageSemaphoreTimeout* is an internal metric that occurs after a bounding timeout reading or writing storage. *AlarmStorageSemaphoreTimeout* can occur if Datomic is waiting tens of seconds to hear back from a storage call, and will likely correlate with spikes in [documented metrics](#transactor-metrics) such as *StorageGetMsec* or *StoragePutMsec*. After an *AlarmStorageSemaphoreTimeout*, check for problems in storage or the network between a Datomic process and storage.