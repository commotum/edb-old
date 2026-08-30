---
title: "Datomic Deployment"
site: "Datomic"
source: "https://docs.datomic.com/operation/deployment.html"
description: "Learn how to manage Datomic deployments."
word_count: 3130
---

This guide includes advice for managing Datomic deployments:

## Getting Connected

A Datomic deployment consists of a storage service, an active transactor, a standby transactor, and one or more peers. These processes discover each other as follows:

- A transactor process discovers storage via configuration specified in the transactor properties file
- The active transactor then writes its location (an IP address) into storage as part of the heartbeat
- A second transactor will stay in standby mode unless the active transactor misses two heartbeats
- Peers connect to storage using the URI or map specified in their call to connect, read the location of the active transactor from storage, then connect to the transactor

To provision a Datomic system and get it up and running, follow the steps in the [storage](https://docs.datomic.com/operation/storage.html) services topic.

## Getting Reconnected

Processes become unavailable for various reasons:

- A process or machine crashes
- A process loses network connectivity to other processes
- A process stops operating in a timely manner
- An operator kills a process deliberately, e.g. as part of an upgrade

Datomic handles all these causes of unavailability in a unified way described below.

### Transactor Failover

Run a standby transactor to minimize loss of availability. Instructions for running a standby transactor can be found in the [high availability](https://docs.datomic.com/operation/ha.html) section.

Transactor processes are designed to fail fast and allow a (hopefully healthier) process to take over.

A transactor failover is not a bug, but frequent failovers indicate an operational problem.

### Monitoring Heartbeat

If you are using CloudWatch to [monitor transactors](https://docs.datomic.com/operation/monitoring.html), you will see a *HeartbeatMsec* metric for every heartbeat from an active transactor, and you will see a *HeartMonitorMsec* metric for every check made by a standby transactor.

> Note that you can use CloudWatch metrics as you want, regardless of which storage you are using, even if no other part of your application is running in the cloud.

### Peer Failover

The peer library is designed to survive and automatically reconnect in the event of transactor failure, check [transactor failover](https://docs.datomic.com/operation/deployment.html#run-standby-transactor).

When a peer cannot communicate with a transactor or storage, that peer will automatically attempt to reconnect, with no special action required by the application code. During a period of unavailability, calls to [(d/db)](https://docs.datomic.com/clojure/index.html#datomic.api/db) will return the most recent [consistent](https://docs.datomic.com/transactions/acid.html#consistency) database value available locally in the Peer, so that peers can continue to read and query during transactor outage.

The mentioned queries have a sound point-in-time basis, accessible through the database future returned by [(d/sync)](https://docs.datomic.com/clojure/index.html#datomic.api/sync), which will block until the transactor becomes available. A peer will require the latest database value – these techniques allow peers to choose to continue reading during transactor failover or to wait until the transactor becomes available.

Peer application code should be prepared for the possibility of transient write failure in the event of transactor unavailability and implement appropriate retry strategies. Datomic will not automatically retry arbitrary transactions. Not all transactions are idempotent, and transactions may have succeeded, even if the communication back to the peer failed.

Given that a peer must discover on reconnect whether or not a transaction has succeeded, you can handle retry logic with a variety of strategies. In general, you will want to query the database to determine what happened from a time basis after the transaction failure/success. Some example strategies:

- Coordination using [(d/sync)](https://docs.datomic.com/clojure/#datomic.api/sync)
- [Annotating transactions](https://docs.datomic.com/transactions/transaction-processing.html#reified-transactions) with a unique identifier
- Implementing retry in a [transaction function](https://docs.datomic.com/transactions/transaction-functions.html)

## Troubleshooting

> When in doubt, [open a ticket](https://support.cognitect.com/hc/en-us). The Datomic team is here to help.

Use logs and metrics to monitor your system and gather the information necessary to characterize the problem you're seeing. Below is a list of common configuration problems and their typical solutions.

### Do You Have Enough Memory?

Datomic uses timeouts in several places:

- An active transactor that is too slow to write its heartbeat will self-destruct and be replaced by a standby transactor
- If the peer is too slow to ping the transactor (see *datomic.peerConnectionTTLMsec*), the peer will renegotiate the connection with the transactor

If timeouts occur even though the network and storage layers seem healthy, the most likely cause is long garbage collection (GC pauses) caused by a process not having enough memory. When looking for the culprit, keep the following points in mind:

- Peers run arbitrary application code and are far more likely than transactors to encounter memory issues.
- Transactor memory issues are usually traceable to oversized transactions or badly behaved transaction functions. Check [performance and security](https://docs.datomic.com/transactions/transaction-functions.html#performance-and-security).

If a process has a memory problem that you do not yet understand, do *not* conceal it by configuring the process with more memory. While this will temporarily solve the issue you have encountered – it will not fix an actual leak. You will find yourself a much larger memory dump to study when symptoms manifest.

### Are Your Processes Isolated?

Datomic is a distributed system. Storage services, transactors, and peers are designed so that load on one process has minimal impact on other processes (while delivering on Datomic's semantic promises).

As a convenience for development, you can *undistribute* Datomic by running more than one process on the same virtual or physical hardware, e.g. running storage, transactor, and peer on a developer laptop. This *development mode* cannot deliver reliability or performance of a production deployment, as processes are competing for a single resource (the machine), and a single machine failure brings all processes down.

System tests and production should be performed against a properly distributed installation, with each process running on its own (possibly virtual) hardware. The Datomic team does not support troubleshooting performance or reliability problems on non-distributed installations of Datomic.

### Transactor Fails to Start

If a transactor process fails to start, it is likely misconfigured. Check the logs for a WARN, ERROR, or FAIL level message, which should point to the problem.

If the transactor that fails to start is a [CloudFormation transactor](https://docs.datomic.com/operation/aws.html) on AWS and it fails to rotate logs, make sure:

- That your memory settings are valid (e.g. heap fits within instance size)
- That you are using a supported AWS instance type (e.g. Datomic requires a writable file system)
	- Supported instance types can be found in a Datomic-generated cloud formation template under the key "AWSInstanceType2Arch"

If the transactor fails to write heartbeat at launch, verify that the storage connection information Datomic has can be used to reach the storage, e.g. by connecting directly with *psql*, *cqlsh*, etc.

### Transactor Starts Then Fails Later

If a transactor fails after running successfully for some time, the most likely causes are storage unavailability or a pause/failure of the transactor process.

In the case of storage unavailability, the root causes can be:

- A network partition/reachability issue
	- Between transactor and storage
		- Between nodes in a distributed storage
- Storage latency
	- Use *StoragePutMsec* and *StorageGetMsec* metrics to diagnose
		- Back pressure from exceeding DynamoDB provisioning
- Latency/pauses on storage introduced by:
	- Virtualization or GC pauses on storage instance(s)
		- Storage ops, such as log replication, backup, or VACUUM events
- Failure of an eventually consistent storage to provide a transactional or consistent write with sufficiently low latency to Datomic when required

In the case of a pause on the transactor, the likely root causes are:

- JVM Garbage Collection: the most common cause is a transactor configuration that has had its defaults overridden by a custom transactor launch script or arguments passed to the transactor other than *Xmx* or *Xms*. Make sure in these cases to include the default *\-XX:+UseG1GC -XX:MaxGCPauseMillis=50* JVM GC related args.
- Virtualization/VM level pause:
	- These can usually be discerned from pauses in logs that are also correlated with pauses in other system logging, but are otherwise difficult to diagnose directly.
		- These may turn out to be an unalterable reality of your data center and need to be accomodated by higher values for *heartbeat-interval-msec*.
- Interactions with other processes on the same box (not a supported config).

### Peer Fails to Connect

Peers must connect to storage and then to the transactor. Failures to connect to storage will appear as errors thrown by the storage driver for that particular storage, e.g. an *org.postgresql.util.PSQLException* on Postgres. Failures to connect to the transactor will be ActiveMQ errors.

> If a peer can't connect to a transactor, the most likely culprit is transactor configuration, not peer configuration.

The transactor writes the values of *host* and *alt-host* to storage as part of the heartbeat. The peer will attempt to connect to *host* first, then *alt-host*. A peer must be able to reach the transactor at one of these to connect to it.

To verify that the transactor machine can be reached from the machine the peer runs on, use one of the values it sets for *host* and *alt-host*.

> The most common cause of peer connection failure at startup is the lack of or misconfiguration of the *host* and *alt-host* values.

If you run in a containerized or dynamic environment, you may have to generate the transactor's *host* and *alt-host* values at launch (e.g. with scripts or command line tools that can pipe in some discoverable values for the *host* or *alt-host* setting).

If you've ruled out the transactor and storage, other likely suspects are *peer network configuration* or *peer permissions to connect to the transactor*.

### Peer Connects Then Fails Later

If a peer connects to the transactor and runs for some time, failing the connection to the transactor later, do the following:

- Correlate with transactor logs or metrics. Was there a transactor failover, a large number of transactions runs, an increase in transactor load, etc.
- Look at peer and transactor memory use - ActiveMQ failures can be caused by memory pressure.
- Be suspicious of GC pauses on both the transactor and the peers. As peers run application code, they're just as or more likely to experience GC pauses as the Transactor. We recommend troubleshooting suspected GC pauses by collecting logs using these JVM args:

```
-Xloggc:{file} -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCCause
-XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCDateStamps
```

- If you identify garbage collection as a culprit, you can look to either:
	- Tune your application and/or its JVM GC settings to mitigate lengthy garbage collection pauses.
		- Increase *heartbeat-interval-msec* and *datomic.peerConnectionTTLMsec* to make the peer-to-transactor connection more tolerant of GC pauses.

## Common Problems

### Unsupported protocol

Attempting to connect to a DB without the proper dependencies will throw an error indicating the protocol is unsupported.

Unsupported protocol error:

```
CompilerException java.lang.IllegalArgumentException: :db.error/unsupported-protocol Unsupported protocol :dev, compiling:(core.clj:36:11)
```

### Misconfigured Data Directory

Transactors use the *data-dir* specified in the transactor properties file as the storage location for the dev and free storage protocols as well as for temporary storage with all storage protocols. The *data-dir* path must be writable by the Datomic process and the appropriate filesystem permissions must be granted to allow Datomic to create additional temporary directories within the *data-dir*.

Insufficient write and/or create-directory permissions can result in the error:

```
WARN  default    datomic.index - {:message "merge-db failed", :pid 11316, :tid 22}
java.io.IOException: No such file or directory
    at java.io.UnixFileSystem.createFileExclusively(Native Method) ~[na:1.7.0_101]
```

### Max Threads Error When Enabling Health Endpoint

After enabling [Health Check Endpoint](https://docs.datomic.com/operation/transactor.html#health-check-endpoint) the transactor fails to start with:

```
java.lang.RuntimeException: Unable to start ping endpoint localhost:9999
...
...
...
Caused by: java.lang.IllegalStateException: Insufficient threads: max=3 < needed(acceptors=1 + selectors=2 + request=1)
```

In some containerized deployments, there is an issue in thread calculation when Health Check Endpoint is enabled. To address this issue, you can manually configure and set threads using the `ping-concurrency` property (set in the transactor properties file).

### Storage-Level Problems

If the storage service fails to read or write data, Datomic will throw an exception on any data access that needs to read a segment that the underlying storage cannot return. In particular, warnings in the transactor log of this type:

```
WARN default datomic.index - {:tid 645868, :pid 20809, :message "merge-one-index failed"} 
java.lang.Exception: Key not found:
```

indicate that the key provided in the error cannot be returned by the underlying storage. For eventually consistent backend storages (i.e. Cassandra, DynamoDB), the loss may be temporary and self-resolving (this is frequently the case during node rebalancing).

### Peers Fail to Connect to Transactor

Datomic uses storage to coordinate transactor location. The network location of the transactor is specified to the transactor via the *host* and *alt-host* values in the transactor properties file. Peers connect to storage, lookup the transactor endpoint, and then connect to the transactor.

Peers must be able to reach the transactor at the address specified by either the *host* or the *alt-host* property. The lack of proper entries for *host* and *alt-host* in the transactor properties file will result in the following error upon attempting to connect a peer:

```
Caused by: clojure.lang.ExceptionInfo: Error communicating with HOST 0.0.0.0 on PORT 4334 {:alt-host nil, :peer-version 2, :password "...", :username "...", :port 4334, :host "0.0.0.0", :version "0.9.5130", :timestamp 1449708475587, :encrypt-channel true}
   at clojure.core$ex_info.invoke(core.clj:4593)

   Caused by: ActiveMQException[errorType=NOT_CONNECTED message=AQ119007: Cannot connect to server(s). Tried with all available servers.]
```

Further description of the peer connection process is available [in the transactor reference page](https://docs.datomic.com/operation/transactor.html#connecting-to-transactor) and [the peer fails to connect section](#peer-fails-to-connect).

### Queries Fail With Thread Pool Shutdown

Calling the Datomic API [*shutdown*](https://docs.datomic.com/clojure/#datomic.api/shutdown) or Clojure *shutdown-agents* function will shut down the Peer library. If either of these functions is called before an attempt to run a Datomic query, the query will fail with the exception:

```
Task java.util.concurrent.FutureTask@7ff48e13 rejected from java.util.concurrent.ThreadPoolExecutor@3407eed0[Shutting down, pool size = 7, active threads = 7, queued tasks = 0, completed tasks = 7]
        datalog.clj:1454 datomic.datalog/eval-rule[fn]
        datalog.clj:1434 datomic.datalog/eval-rule
```

## Upgrading Datomic Schema

Every Datomic database starts with a base schema, whose identifiers are namespaced with `:db`. When the Datomic team enhances the base schema, all databases created with new versions of Datomic get the enhancements automatically.

Existing databases do **not** automatically incorporate enhancements to the base schema. To upgrade existing databases to use new base schema, first upgrade **all** transactors and peers to the minimum version of Datomic required for the base schema features you want:

| Feature | Minimum Datomic version | Released |
| --- | --- | --- |
| Tuples | 0.9.5927 | June 27, 2019 |
| Attribute predicates | 0.9.5927 | June 27, 2019 |
| Entity specs | 0.9.5927 | June 27, 2019 |
| `:db.type/symbol` | 0.9.5927 | June 27, 2019 |
| `:db.tuple/discontinued` | 1.0.7705 | July 10, 2026 |

Once you have upgraded all transactors and peers, you can upgrade an existing database to use the latest base schema by passing the `:upgrade-schema` action and a database URI to `administer-system`. For example:

```
(d/administer-system {:uri "datomic:dev://localhost:4334/my-db"
                      :action :upgrade-schema})
```

Upgrading a schema is an idempotent operation, so it will not harm your data to call it more than once. That said, you should treat `administer-system` as a potentially expensive operation. Call it only when you need it, not before every call to `connect`.

> Once you have used a base schema feature in a particular database, do **not** run any peer or transactor process on versions of Datomic older than the features used. Check the table above.

## Upgrading a Live System

It is possible to update both peers and transactors live in a running system, subject to the constraints described below. If the update you are performing is a compatible one, you can update the transactor and the peers piecemeal in any order you choose, without ever taking down the entire system.

> Always test a new release of Datomic in a staging environment before upgrading your production environment.

To upgrade the transactor, start a new transactor (or pair of transactors) based on the release of Datomic you are upgrading to. Once these processes are up and monitoring the storage heartbeat, stop the old transactors and the new ones will automatically take over.

To upgrade peer applications, simply start new peers and stop old ones. You can stagger the ups and downs to maintain availability during the upgrade.

### Testing Upgrades in a Staging Environment

It is always a good idea to test new releases of Datomic in a staging environment before upgrading to production, as follows:

- Make your staging system as similar to production as you feasibly can
- Make sure that your staging and production environments are fully separated
	- Do not connect to both the staging and production system from the same peer (and you should enforce this with firewalls, AWS roles or security groups, etc.)
		- Do not use the same storage instance/cluster for staging and production

With a suitable staging configuration, follow these steps to test a Datomic upgrade:

- [Backup](https://docs.datomic.com/operation/backup.html) the production system
- Restore it into the storage used by your staging system
- Start your staging system including transactor and peers
	- Use the same upgrade procedure as you plan to on production, e.g. rolling upgrades of peers
- Verify that your application(s) work correctly

### Monitoring Upgrades

When upgrading a Datomic system's transactors, monitor metrics to determine when you can safely failover to the new transactors. In particular, watch for *HeartMonitorMsec* sum and count (data samples) metrics to be reported as any new transactors will launch as standby.

For example, with *heartbeat-interval-msec* defaults of 5000, you will see a count of *12* and sum of around *60000* for both *HeartbeatMsec* (on the active transactor) and *HeartMonitorMsec* (on the standby transactor). If you launch two additional transactors with the upgraded version of Datomic you will see the *HeartMonitorMsec* count change to ~36 and sum increase to ~/180000/ – assuming that metrics are reported to an aggregator in the same namespace. Otherwise, e.g. if you monitor transactors individually, you will see *HeartMonitorMsec* metrics reported by each transactor when it is up and healthy.

When you see *HeartMonitorMsec* reported by the newly launched transactor(s), you are safe to stop the old transactor processes/VMs and failover to the upgraded transactor(s). When the upgraded transactor becomes active it will begin reporting *HeartbeatMsec* metrics. In the failover window, you may see *Transactor unavailable* error messages on the peers which will resolve when the new transactor has taken over. These should go away when the new active transactor begins heartbeating.

### Compatibility Across Datomic Releases

It is a design objective of Datomic to preserve compatibility across releases to the greatest extent possible. Compatible releases can be used together in a single system, i.e. two (or more) distinct releases of the software running in different processes.

The Datomic [changes page](https://docs.datomic.com/changes/pro.html) contains release notes for every release. The vast majority of these changes are compatible changes, and incompatible changes are highlighted in the text. When migrating between releases, you should always review **all** changes between the two releases.

There have been three compatibility-breaking releases that require that you shut down all Datomic processes on upgrade:

- 0.9.4609 (2014-03-13)
- 0.9.4470 (2014-02-17)
- 0.8.3705 (2013-01-09)

Check the [release notices](https://docs.datomic.com/release-notices.html) page for documentation of all critical release notices.

## Downgrading

Generally speaking, you should never downgrade peers or transactors to older versions of Datomic.

- Newer versions are better: they may contain important fixes
- Newer versions are compatible: they continue to support the entirety of the API
- Our support team is better able to help if you are running a recent version

If you have a need to downgrade Datomic, [contact support](https://www.datomic.com/support.html) first, and let the Datomic team advise you.