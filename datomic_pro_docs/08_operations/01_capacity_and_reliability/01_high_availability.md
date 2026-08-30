---
title: "High Availability (HA)"
site: "Datomic"
source: "https://docs.datomic.com/operation/ha.html"
description: "Learn about high availability in Datomic. Ensure your system is resilient and reliable with our guide to HA setup and configuration."
word_count: 787
---

High Availability (HA) is a Datomic Pro feature for ensuring the availability of a Datomic transactor in the event of a single machine failure.

## Enabling HA

To enable HA, simply run two transactor processes on separate machines, with identical transactor properties files (in particular, configured to use the same storage).

The first transactor to communicate with storage will become active, and the second transactor will operate in standby mode until/unless the active transactor fails.

You can run two transactors in a single stack by setting a property in your CloudFormation template:

```
aws-autoscaling-group-size=2
```

If you are managing your own processes, provide a mechanism that tries to keep two machines running the transactor process at all times.

## Peer Recovery Time

The time for a peer to recover from a transactor failure consists of two components:

- The time for the standby transactor to detect that the active transactor has failed
- The time for the peer to detect the new transactor

The standby transactor takes over if it sees the active transactor miss two consecutive heartbeats. The peers will poll for a new transactor once per second, so the total number of seconds for a peer to recover should be at worst:

```
2 * (heartbeat-interval-msec / 1000) + 1
```

The *heartbeat-interval-msec* is configured in the transactor properties file and the default value of 5000 implies a worst-case peer failover time of around 11 seconds. You can improve this time by choosing a smaller *heartbeat-interval-msec*, and carefully considering two risks:

- Network anomalies can trigger unnecessary failover if they could have resolved themselves in a longer heartbeat window
- Long Java garbage collection (GC) pauses should happen rarely or never on a correctly configured transactor, but could trigger an unnecessary HA failover if heartbeat-interval-msec is tuned too low

You should test Datomic with your environment before changing the heartbeat interval.

The minimum legal value for *heartbeat-interval-msec* is 1000.

## Reads Remain Available

Peers can continue to read and query during a transactor failover. In particular, calls to [datomic.api/db](https://docs.datomic.com/clojure/index.html#datomic.api/db) will neither block nor fail during the window of a transactor failover.

## Transaction Latency After HA Recovery

The current transactor implementation does not replay the log to catch up until **after** an HA event has occurred.

While the transactor is available for peer connections as soon as it takes the active role, transactions queues behind log catch-up, and experience unusually long latencies (up to several seconds) for several seconds after an HA event.

## Application-Level Retry

Datomic does not automatically retry transactions after a transactor failure. Applications, aware of their own semantic needs, can implement an appropriate retry policy, with full access (e.g. via the [Log API](https://docs.datomic.com/reference/log.html)) to recent transaction history.

## Switching to a Different Transactor Version

The Datomic team recommends using normal HA failover to deploy a different transactor version, as follows:

- Start a new transactor pair running the version of Datomic you are switching to.
- Perform whatever operational checks are appropriate in your environment to make sure the new process pair appears healthy. For example, with AWS you could verify the increase in *HeartMonitorMsec* count.
- Stop the original standby transactor.
- Stop the original active transactor.

It is ok to perform the last two steps simultaneously.

## Moving Across Data Centers / Regions

You may want to move a Datomic system from one data center or region to another, e.g. to recover from the catastrophic failure of a data center. To do so, perform the following steps, in this order:

- Shutdown all Datomic-related processes (transactors and peers) in the data center you are leaving
- Restore a **consistent** copy (see below) of the Datomic data into the new data center
- Start all Datomic-related processes in the new data center

### Use Datomic Backup For Consistent Copy

Datomic's [backup](https://docs.datomic.com/operation/backup.html) makes a *consistent copy* of Datomic data, i.e. backup preserves Datomic's consistency guarantees. Thus Datomic backup is suitable for use in a disaster-recovery plan.

### Other Consistent Copy Options

Datomic treats [storage](https://docs.datomic.com/operation/storage.html) as a pluggable component, which raises the possibility of copying Datomic data using the underlying storage engine's backup or replication facilities, an approach hereafter called "storage copy". Storage copy may be preferable to Datomic backup for disaster recovery for reasons including:

- Operators are already familiar and comfortable with the tools involved
- The disaster recovery strategy needs to protect data placed in storage by other applications besides Datomic
- Storage facilities provide live replication that can reduce recovery time

A storage copy is a consistent copy if and only if it always comprises a complete set of writes made up to a specific point in time. For example:

- SQL log shipping makes a consistent copy and is suitable for disaster recovery
- Replication or backup of eventually consistent storage cannot (by definition) make consistent copies and is **not** suitable for disaster recovery