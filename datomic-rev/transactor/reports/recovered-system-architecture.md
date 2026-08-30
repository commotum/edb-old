# Recovered system architecture

## Purpose and boundary

This is the teaching map for the recovered Datomic Pro 1.0.7277 Peer and
Transactor. It explains the supported PostgreSQL-backed system from recovered
source and binds important state transitions to retained runtime evidence. It
does not treat every source-visible optional facility as validated, and it does
not require universal equivalence of dormant Peer/Transactor overlaps.

The candidate boundary contains recovered Peer, core2-support, and Transactor
source roots; 52 recovered Transactor Java classes; ordinary hash-bound
distribution dependencies; and one content-addressed sanitized Nano
derivative. Original Peer, core2, Transactor, and Nano implementations are
forbidden from candidate classpaths. The full construction and isolation rules
are in [the PostgreSQL vertical-slice report](postgresql-vertical-slice.md).

At a high level, the recovered components compose as follows:

```text
application
  -> recovered Peer API / immutable Database values
  -> coordination lookup and Artemis transport
  -> recovered Transactor master
  -> transaction pipeline and per-database log descriptor CAS
  -> PostgreSQL datomic_kvs authority
  -> transaction notification / Peer basis advance
  -> persistent index roots and restart catchup
```

## Configuration, startup, and readiness

### Configuration contract

[`datomic.launcher/run-transactor`](../src-clj/datomic/launcher.clj#L47)
keywords the Java property names and then fills only absent license, SQL-user,
and SQL-password values from the corresponding ambient system properties. The
property file therefore wins when both sources specify a value; the retained
gates exercise file-supplied SQL credentials, not the ambient credential path.
The resulting property map is normalized in
[`datomic.transactor/ensure-args`](../src-clj/datomic/transactor.clj#L263).
That function installs defaults, translates selected transactor keys into
system properties, resets the recovered configuration table, converts the
protocol to a keyword, rejects missing required fields, parses numeric ports,
and constructs the normalized argument map. For `:sql`, the required core is:

```text
protocol, host, port,
memory-index-threshold, memory-index-max, object-cache-max,
sql-url, sql-driver-class
```

SQL username/password and validation query are optional at this normalization
layer and are carried when present. The recovered
[`datomic.config/config-table`](../src-clj/datomic/config.clj#L206) supplies
typed coercion, predicates, and defaults. `validate-properties` enforces the
memory-index ordering and the combined 75%-of-JVM-memory ceiling
([source](../src-clj/datomic/config.clj#L369)). Invalid known values and missing
protocol-required fields fail as configuration arguments; this slice makes no
claim that every extra property key is rejected.

For the supported path, [`datomic.transactor/run*`](../src-clj/datomic/transactor.clj#L723)
turns the normalized SQL arguments into the system cluster configuration with
an initial pool size of four. [`datomic.kv-sql-ext`](../src-clj/datomic/kv_sql_ext.clj#L72)
constructs and tests the pool, using the configured validation query or the SQL
default `select 1` and test-on-borrow semantics. The retained runner supplies a
fresh catalog, dedicated non-superuser/scoped roles, explicit JDBC
URL/driver/user/password/validation query, bounded memory settings, data
directory, PID file, service address, and exercised heartbeat intervals of
5000 or 8000 milliseconds. Runner-generated node passwords are confined to
mode-0600 runtime property files outside the manifest evidence and are
generically redacted and literal-scanned before a gate may pass; retained
configuration records hashes, not plaintext. Grant minimality and the ambient
credential path are not claimed.

### Startup state machine

Startup is intentionally asynchronous:

```text
properties
  -> ensure-args: coerce/default/validate
  -> run*: claim PID file, initialize storage protocol and optional hooks
  -> start lifecycle thread
  -> print "System started" (launcher scheduled; not readiness)

lifecycle thread
  -> read coordination state
  -> expected-revision heartbeat CAS
     -> success: invoke serve asynchronously
        -> create Artemis server + transaction master
        -> service port opens
     -> storage error / timeout / conflict: no beat
        -> lifecycle critical failure
        -> registered shutdown handlers and bounded process exit

first database use
  -> Peer sends start-database
  -> resolve catalog/database storage configuration
  -> ensure index and log
  -> claim the per-database log descriptor
  -> acknowledge database started
  -> load index and catch up descriptor-referenced log
  -> start remaining database workers and serve work
```

The source ordering matters. `run*` calls `start-lifecycle` and then prints
`System started` without waiting for ownership or transport publication
([`datomic.transactor`](../src-clj/datomic/transactor.clj#L961)). The lifecycle
starts its worker thread and returns `:started`
([`datomic.lifecycle/start`](../src-clj/datomic/lifecycle.clj#L159)). Only a
successful heartbeat invokes `serve`, whose recovered implementation creates
the Transactor master
([`datomic.transactor/start-lifecycle`](../src-clj/datomic/transactor.clj#L77)).
The master starts the Artemis server and session/RPC infrastructure
([`datomic.update/create-master`](../src-clj/datomic/update.clj#L3344)).

Database readiness is a second layer initiated by the recovered Peer's
`start-database` request
([`datomic.peer`](../../src-clj/datomic/peer.clj#L683)).
`internal-start-database` obtains the catalog, creates the database cluster,
ensures and claims the log, then delivers `:status :started` before index/log
catchup and the remaining database workers complete
([`datomic.update`](../src-clj/datomic/update.clj#L2669),
[`acknowledgement ordering`](../src-clj/datomic/update.clj#L2838)). Failure to
claim the log is process-fatal. There is consequently no single atomic
"fully database-ready" event in this source path. This separates three signals
that should not be confused:

1. `System started`: the launcher scheduled lifecycle work;
2. open service port: coordination ownership succeeded and the master transport
   was constructed; and
3. successful Peer workload: endpoint discovery, database initialization/log
   claim, transport, and application-visible state all work together.

### Retained operational evidence

No new execution was required for this slice because both sides of the critical
startup boundary are already retained.

- **Successful current-source startup.** The accepted partition run at
  `/tmp/datomic-recovered-pair-ha-partition-v6` records an exact owned PID,
  start time, argv, property path, `System started`, service-port readiness, and
  a recovered-Peer seed at basis 1001 with 64 logical rows. Its 126-entry
  manifest verifies at SHA-256
  `4e0a4ca8c8c58863712f8252dd66e320756dd70a02a38288f320ecf924299272`.
  Later same/fresh-Peer agreement and clean shutdown show that readiness was not
  a transient port-only observation.
- **Repeated startup.** `/tmp/datomic-recovered-pair-live-v3` records three
  owned Transactor boots against one PostgreSQL catalog, SQL validation,
  coordination heartbeat and port publication, equal seed/restart snapshots,
  and three bounded `SIGINT`-only process stops. Its 108-entry
  `evidence.sha256` verifies at SHA-256
  `100245c7dbb8639ea4bbc8ad46594818bfb9909e6aae75a3b79c1b26263d24bf`;
  `run-status.properties` is `passed` and no owned service remains.
- **Fatal storage/schema startup.** The focused run at
  `/tmp/datomic-recovered-pair-startup-failure-v1` starts from a fresh catalog
  that deliberately lacks `datomic_kvs`. It retains the PostgreSQL
  `PSQLException` through 20 bounded storage retries, emits exactly one
  lifecycle-thread failure, never opens the service port, never degrades into a
  secondary `NullPointerException`, preserves exact PID ownership, remains
  controllable by bounded `SIGINT`, leaves PostgreSQL responsive, and shuts it
  down cleanly. `System started` can appear in this failed run, directly proving
  that the marker is not readiness. Its 69-entry manifest verifies at SHA-256
  `40a45828557f88f9677f0926b992d639461c01f8f5a322344190d57fc7ac573b`.

The supported operational contract is therefore: configuration must normalize
and validate; storage-backed coordination must succeed before the service port
opens; and a Peer workload is the end-to-end readiness signal. Invalid-property
combinatorics and every optional protocol are not separately executed because
they do not materially challenge this PostgreSQL core contract.

## Shutdown, restart, and recovery

### Two distinct stop paths

The retained runner's word `graceful` has a deliberately narrow mechanical
meaning. [`stop_transactor`](../scripts/validate-postgresql-vertical-slice.sh#L1689)
revalidates the exact PID/start-time/executable/argv identity, sends `SIGINT`,
allows 20 seconds for exit, and marks `sigint.graceful=true` only when neither
`SIGTERM` nor `SIGKILL` was needed. It then requires the service port to close.
This is strong process-ownership and external-cleanup evidence; it is not an
application-level marker that every transaction was drained or every
`AsyncShutdown` completed.

Recovered source exposes a separate critical-failure state machine:

```text
heartbeat/storage/background contradiction
  -> first process/fail realizes a permanent failing state
  -> schedule registered failure handlers concurrently
     -> Master shutdown closes session factory, then Artemis server
  -> wait the fixed self-fence interval
  -> System.exit(-1)

operator SIGINT
  -> runner verifies exact process ownership and sends the signal
  -> JVM exits within the bounded window, without escalation
  -> operating system releases port and SQL sessions
  -> runner verifies the closed service port, then stops PostgreSQL
```

[`datomic.process/SharedCriticalFailure`](../src-clj/datomic/process.clj#L85)
makes failure one-way, launches the registered handlers, waits 30 seconds, and
then exits. Once serving constructs a Master, the Transactor registers its
failure handler
([`datomic.transactor/start-lifecycle`](../src-clj/datomic/transactor.clj#L92)).
The Master's `AsyncShutdown` closes the Artemis session factory and then the
Artemis server; the server mapping calls `.stop`
([`datomic.update/Master`](../src-clj/datomic/update.clj#L2620),
[`datomic.artemis-server`](../src-clj/datomic/artemis_server.clj#L193)).

The narrower source boundary matters. A database has its own shutdown protocol
that signals its shutdown reference, stops and joins background components, and
closes the database cluster
([`datomic.update/Database`](../src-clj/datomic/update.clj#L2253)). That protocol
is called when `internal-stop-database` removes a database
([source](../src-clj/datomic/update.clj#L3023)); the Master's process-wide
shutdown does not enumerate its database map. No explicit recovered
`SIGINT`/JVM-shutdown-hook registration connects the operator signal to either
protocol. Accordingly, the supported normal-stop claim is bounded process exit
with released external resources, while partition v6 directly observes the
critical-failure route through heartbeat conflict, Artemis stop, and status
255. It is not an unobserved claim of orderly in-flight draining.

PID files have similarly precise semantics. `claim-pid-file` overwrites the
configured file with the current PID but has no deletion path
([`datomic.process`](../src-clj/datomic/process.clj#L231)). Retained PID files
therefore remain after exit. The runner never treats them as ownership by
themselves: it combines the recorded PID with live `/proc` start time,
executable, and full argv before signaling, and proves the process absent
afterward.

### Restart and authoritative recovery

A new Transactor does not recover authority from its local data directory:

```text
new JVM, fresh data directory
  -> validate SQL and win coordination CAS
  -> resolve catalog and create database cluster
  -> read current persistent-index reference and current log-tail descriptor
  -> claim the log descriptor by expected-revision CAS
  -> acknowledge database start
  -> load the referenced four-family index root
  -> replay only descriptor-referenced transactions after index nextT
  -> install remaining database workers
  -> successful Peer sync/query proves usable recovered state
```

`ensure-index-and-log` finds or initializes the index reference and reconstructs
the log only from its current tail descriptor
([`datomic.log`](../src-clj/datomic/log.clj#L1528)). Startup claims that log,
loads the persistent root, and calls `log/catchup`
([`datomic.update`](../src-clj/datomic/update.clj#L2778)). `load-index` rebuilds
the EAVT, AVET, AEVT, and VAET/RAET views plus history/fulltext pointers from
the referenced root map
([`datomic.index`](../src-clj/datomic/index.clj#L2084)); `catchup` seeks from the
index database's `nextT`, validates and accepts later log transactions, and
records index `t`, tail `t`, and replay bytes
([`datomic.log`](../src-clj/datomic/log.clj#L1440)). As noted above, the start
acknowledgement precedes catchup, so the successful Peer observation is the
completion signal.

Persistent-index publication and recovery are complementary. In the running
process, a completed index is merged with any intervening memory log and swapped
into the database reference
([`datomic.update/process-new-index`](../src-clj/datomic/update.clj#L685),
[`datomic.adopter`](../src-clj/datomic/adopter.clj#L9)). Publication itself is
an expected-revision reference CAS
([`datomic.index`](../src-clj/datomic/index.clj#L5997)). A fresh process simply loads the winning
referenced root; it does not discover losing or unreferenced immutable values.
That same rule explains crash recovery: a prepublication orphan remains
invisible, while a successfully published descriptor is replayed exactly once.

### Retained operational evidence

| Run | Verified recovery boundary |
|---|---|
| `/tmp/datomic-recovered-pair-live-v3` | 108/108 evidence hashes verify. Three distinct PIDs and fresh data directories replay 37,372 bytes to `tail-t 1001` and 64,032 bytes to `tail-t 1066`; seed/restart and augment/restart fingerprints are exact. All three stops return 130 after `SIGINT` with no escalation. |
| `/tmp/datomic-recovered-pair-index-v5` | 113/113 verify at manifest SHA-256 `c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238`. An explicit request publishes index `t 1066`; a third fresh process loads `tail-t = index-t = 1066` with zero replay bytes and an identical Peer snapshot. |
| `/tmp/datomic-recovered-pair-transport-v2` | 123/123 verify at `87eaf2e129aa5d3ec12b4e84a9c3143e623b21767a00ee0c3916ea3c037383f6`. The same Peer connection reports unavailable during a 20-second Transactor pause, reconnects, commits/reads at basis 1099, and agrees with a fresh Peer. |
| `/tmp/datomic-recovered-pair-ack-crash-v2` | 110/110 verify at `6f3a5ab8d6c877d7a9f2f8e2e23c4670b9a2228995dd290f854426a5a20fd0c5`. A prepublication `SIGKILL` leaves the interrupted write absent; two fresh Transactors replay the referenced lineage, accept a recovery write, and agree with a fresh Peer. |
| `/tmp/datomic-recovered-pair-ack-postpublication-v4` | 106/106 verify at `d8ac5e5314603c1bca54aa8a773d60a7eec136e29066b4d220d110e1591374bf`. A descriptor published before the writer crash survives restart; the same Peer's original Future and a second fresh-process audit observe exactly one effect. |

Every row finishes with absent owned processes, closed service/PostgreSQL
ports, and PostgreSQL control state `shut down`; the crash rows additionally
retain an explicit zero Datomic-role session count. No new run is needed for
this slice: together the rows cover bounded normal stop,
pre- and postpublication crashes, positive log replay, zero-replay persistent
index adoption, same-Peer reconnect, and independent fresh-Peer agreement.

Residual operations remain explicit. Same-data-directory reuse, stale-PID-file
service-manager policy, `SIGTERM`, in-flight orderly drain, interrupted index
construction, automatic threshold scheduling, and a post-coordination
partial-Master construction failure are not exercised. None contradicts the
supported PostgreSQL recovery model; none is silently promoted to a validated
guarantee.

## Monitoring and process events

### Local event and metric pipeline

The recovered monitoring path is useful only if its signal types are kept
separate. [`datomic.slf4j/process`](../src-clj/datomic/slf4j.clj#L72) turns a
string into a message map when necessary, adds the JVM PID and current thread
ID, and prints with a 100-element collection limit. The `log-time` macro emits
begin/end records, elapsed milliseconds, and a thrown class when applicable;
selected timed events also feed named statistics such as `TransactionMsec`,
`LogWriteMsec`, and `StoragePutMsec`
([source](../src-clj/datomic/slf4j.clj#L48),
[`metric-expr`](../src-clj/datomic/slf4j.clj#L308)). Direct subsystem calls to
[`datomic.monitor/add-stat`](../src-clj/datomic/monitor.clj#L239) use the same
in-process statistics store.

```text
subsystem state or timed operation
  -> structured SLF4J record with pid/tid
  -> optional named timing/counter observation
  -> interval Statistics accumulator

every 60 seconds, plus one immediate startup report
  -> reset and snapshot interval Statistics
  -> merge JVM runtime and system-cache gauges
  -> log {:event :metrics ...}
  -> invoke the configured metrics callback

critical process failure
  -> add SelfDestruct
  -> emit and invoke one final metrics report
  -> run the remaining failure handlers
```

[`snapshot-statistics`](../src-clj/datomic/monitor.clj#L155) swaps in a new
accumulator before reading the old one, returning each metric as low, high,
sum, and count. Reports are therefore per-snapshot intervals, not process
lifetime totals. [`snapshot-metrics`](../src-clj/datomic/process_monitor.clj#L58)
adds `AvailableMB`, system-cache gauges, and dynamically registered component
metrics. Pro startup constructs the callback, reports once asynchronously,
then schedules reports every 60 seconds
([`start-metrics`](../src-clj/datomic/process_monitor.clj#L164)). It also
registers the final `SelfDestruct` report as a process-failure handler.

For the retained local configuration, `datomic.metricsCallback` defaults to
`clojure.core/identity`
([`datomic.config`](../src-clj/datomic/config.clj#L335)). Callback construction
requires and resolves a namespaced Clojure symbol, or compiles the supported
static-method form ([`datomic.callback`](../src-clj/datomic/callback.clj#L102)).
The logged `{:metrics/started clojure.core/identity}` marker consequently proves
that the local callback was constructed and the reporter was scheduled; it is
not an `:event` record and does not prove delivery to an external monitor.
`datomic.metricEventCallback` is a distinct optional, synchronous callback on
every `add-stat` observation
([`datomic.monitor`](../src-clj/datomic/monitor.clj#L213)); it is not configured
in the accepted runs. CloudWatch selection and any custom external callback
delivery remain outside the validated boundary.
Because `MetricsReport` or `MetricsFail` is added only after the current
snapshot has been passed to the callback, that delivery result appears in the
next report (or the final failure report), not in the payload whose invocation
it describes.

The similarly named [`datomic.process.events`](../src-clj/datomic/process/events.clj#L7)
is not operator telemetry. It is a synchronous, in-JVM subscriber map keyed by
an event keyword. Its recovered core use publishes `:datomic.garbage/mark`
after log-tail/index publication and routes it to the garbage marker
([publisher](../src-clj/datomic/update.clj#L1629),
[`install-mark-handler`](../src-clj/datomic/garbage.clj#L366)). Direct
subscribe/unsubscribe/delivery behavior has no retained event record and is
classified coherent but unexercised here; its externally meaningful effects
belong to the bounded-maintenance slice.

### Interpreting lifecycle signals

| Signal | Supported interpretation | What it does not prove |
|---|---|---|
| `System started` and `:transactor/start` | Launcher/lifecycle scheduling and a sanitized normalized-argument snapshot. | Coordination ownership, an open transport, or database readiness. |
| `:transactor/standby` | One observation of the coordination revision/timestamp and its current missed-heartbeat count. | Writer authority or an open service port. |
| `:transactor/heartbeat` | The endpoint's expected-revision `pod-coord` CAS returned `:ok`; its revision is authoritative at that transition. | That Artemis has already opened or that a database has claimed its log. |
| Artemis `Server is now live` plus an owned open port | The selected process constructed and exposed transport. | Per-database catchup or Peer-visible readiness. |
| `:log/catchup` | Measured bytes plus the resulting log tail/index basis for a database startup. | By itself, a successful client operation. |
| `:update/adopted-index` | An enabled asynchronous observer reports segment/datom gauges after the running database has accepted a completed index. | The exact mutation instant or fresh-process adoption of that root; zero-replay restart plus Peer equality supplies the latter proof. |
| `:transactor/heartbeat-failed :cause :conflict` followed by process termination, final metrics, and Artemis stop | The stale process lost its next coordination CAS and traversed the recovered self-fence path. | Universal fencing under arbitrary partitions or credentials. |

The authority distinction is source-backed. `lifecycle/pump` logs a heartbeat
only after `cluster/set-ref` returns `:ok`, while failure logs the result without
a heartbeat ([`datomic.lifecycle`](../src-clj/datomic/lifecycle.clj#L32)). The
standby loop logs before sleeping and promotes only after two unchanged
observations by attempting that same CAS
([`datomic.lifecycle-ext`](../src-clj/datomic/lifecycle_ext.clj#L76)). The
catchup record is emitted after applying the replayed transactions and reports
the resulting state, not a runner inference
([`datomic.log`](../src-clj/datomic/log.clj#L1440)). Index adoption mutates the
database reference first; when `datomic.indexMetrics` is enabled (the default),
an asynchronous observer later calculates gauges and emits
`:update/adopted-index` ([`datomic.update`](../src-clj/datomic/update.clj#L685)).

### Redaction boundary and retained evidence

Recovered logging uses targeted controls rather than a universal secret
sanitizer. `:transactor/start` removes SQL URL/user/password, transport
password, memcached password, and listed cloud/storage secrets before logging
([source](../src-clj/datomic/transactor.clj#L986)). Heartbeats select endpoint
host/port/version/encryption/timestamp and the ephemeral transport username,
but omit its password. The coordination value used by Peer discovery still
contains both transport username and password, so raw coordination rows and
database dumps are secret-bearing evidence. The earlier `:config/properties` record applies
`logger/redact` only to the exact `datomic.memcachedPassword` key
([source](../src-clj/datomic/transactor.clj#L282)); `logger/redact` itself masks
only caller-selected keys ([source](../src-clj/datomic/slf4j.clj#L567)), and
`sanitize-uri` recognizes only AWS query credential names. Third-party logs are
separate again. No report should therefore describe a Transactor log as
generically secret-free.

The accepted partition runner supplies the independent evidence boundary: node
SQL passwords live only in mode-0600 runtime property files excluded from the
manifest, manifest-eligible text is generically redacted, and a final literal
scan rejects either node password anywhere in the retained evidence. The raw
negative-login diagnostic remains outside that evidence while the retained
assertion records only its reason and pass/fail result.

No new process run was needed. The fully verified index-v5 manifest (113
entries; manifest SHA-256
`c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238`)
contains, on all three boots, identity-callback initialization, the
`metrics/started` marker, an immediate metrics snapshot, standby/heartbeat,
Transactor start, Artemis live, and `log/catchup`. Boot two additionally records
positive 37,372-byte catchup and `:update/adopted-index`; boot three records
`tail-t = index-t = 1066` with zero replay.

The fully verified partition-v6 manifest (126 entries; manifest SHA-256
`4e0a4ca8c8c58863712f8252dd66e320756dd70a02a38288f320ecf924299272`)
adds repeated 60-second interval reports on active and standby nodes and binds
the critical transition exactly: A logs heartbeat conflict, process
termination, a final interval containing `AlarmHeartbeatFailed`, `Alarm`, and
`SelfDestruct`, then Artemis stopped; B has already won the next heartbeat and
serves the agreeing same and fresh Peers. These observations validate local
event production, interval aggregation/default callback invocation, and the
critical-failure event chain. External delivery, the per-stat callback, ping
endpoint, and S3 log rotation are coherent source surfaces but unexercised.

## Cache layers and authority separation

### Caches never own mutable authority

The supported cache contract follows from the storage representation. Mutable
coordination, catalog, log-tail, and index-root references carry revisions and
remain PostgreSQL reads or expected-revision CAS operations. Their referenced
payloads are immutable values addressed by UUID. Only that immutable value
side is eligible for the shared raw-value and deserialized-object caches:

```text
PostgreSQL reference key + revision  <--- always authoritative
             |
             v
       immutable value UUID
             |
             +-> optional memcached/valcache raw-value stack
             |        miss/timeout -> PostgreSQL far store
             |
             +-> per-JVM deserialized object cache
                      miss/eviction -> immutable-value lookup
                         |
                         +-> root directory / weak segment memoization
```

[`CombinedCluster`](../src-clj/datomic/combined_cluster.clj#L20) makes that
separation executable: pod/ref reads, CAS updates, deletion, and database
identity delegate to the original PostgreSQL `ref-cluster`; only immutable
value get/create/delete operations delegate to the cache-aware `val-cluster`.
`create-system-cluster`, used for lifecycle coordination, is never wrapped at
all. A database cluster is wrapped only when the optional `kv-cache-ref` is
non-nil ([`datomic.coordination`](../src-clj/datomic/coordination.clj#L122)).
Thus neither local nor remote cache state can select an active Transactor,
publish a log descriptor, or choose an index root.

### Per-JVM object lookup

Peer and Transactor each lazily create one process-wide Caffeine object cache.
It is weighted by the recovered memory-size estimate of key plus value and uses
90% of `datomic.objectCacheMax`, with the weights scaled to Caffeine's integer
range ([Peer `datomic.domain`](../../src-clj/datomic/domain.clj#L110),
[Transactor source](../src-clj/datomic/domain.clj#L120)). The accepted runs
explicitly configure 128 MiB. The property otherwise defaults to half the JVM
maximum heap, has a 32-MiB minimum, and participates in the startup rule that
`objectCacheMax + memoryIndexMax` may not exceed 75% of maximum heap.

The object-lookup state machine is:

```text
lookup immutable UUID
  -> object-cache hit: return the deserialized object
  -> miss: install/find a per-key in-flight delay
       -> one owner reads the raw value and deserializes it
       -> concurrent waiters share that result
       -> success enters the weighted object cache
       -> failure removes the in-flight delay so a later call can retry

deserialization failure below the object cache
  -> repeat get-val2 with reset-cache=true
  -> accept the newly decoded immutable value, or raise a contextual
     conversion error after the second failure
```

The routing, miss population, and mutation contracts are in
[`datomic.cache/lookup-cache`](../../src-clj/datomic/cache.clj#L138); same-key
collapse and failure cleanup are in
[`lookup-with-inflight-cache`](../../src-clj/datomic/cache.clj#L169). Peer
connection construction installs `system-cache-olookup` as the database object
lookup ([`datomic.peer`](../../src-clj/datomic/peer.clj#L899)); the Transactor
passes the equivalent factory to every database Master
([`datomic.transactor`](../src-clj/datomic/transactor.clj#L98)). On a decoded
lower-cache failure, `DeserializingRepairingLookup` uses `reset-cache` to bypass
the optional raw near store before it reports corruption
([source](../../src-clj/datomic/domain.clj#L197)).

Eviction and clearing therefore change residency and I/O, not database truth.
`Peer.administerSystem {:action :release-object-cache}` clears this JVM-global
object cache and returns `:completed`
([`datomic.peer`](../../src-clj/datomic/peer.clj#L1341)); it does not mutate any
PostgreSQL reference. That thin administrative dispatch is source-coherent but
is left for the bounded-maintenance slice rather than claimed from cache-hit
telemetry.

Each lookup records `ObjectCache` as `1` for a hit or `0` for a miss; directory
misses also increment `DirLoads`. `ObjectCacheCount` is the current estimated
Caffeine entry count
([`datomic.cache.caffeine`](../../src-clj/datomic/cache/caffeine.clj#L65)). The
interval `ObjectCache` sum/count is consequently a hit numerator and probe
count, not a lifetime cache-size counter. Read-ahead first checks the cache and,
when `datomic.readAheadPool` is positive, submits only a miss; disabling the
pool changes prefetch, not lookup semantics
([`datomic.cache`](../../src-clj/datomic/cache.clj#L43)).

### Index, query, and correlation caches

Index-array caches are a second local accelerator, enabled by default. A loaded
immutable root can strongly memoize directory objects in its directory array;
a loaded directory holds segments through weak references. An absent directory
or a cleared segment reference falls through to the ordinary UUID object
lookup and may repopulate the slot
([`get-dir-node`](../../src-clj/datomic/index.clj#L231),
[`Index.seek`](../../src-clj/datomic/index.clj#L1157)).
`datomic.useIndexArrayCaches=false` therefore trades retention for additional
object lookups; it does not select different datoms. The Peer memory index is
not one of these evictable caches: it is logical database state advanced by
transaction notifications and reconciled when adopting a persistent index.

The Peer query cache is different again: a 1,000-entry computing cache maps a
query form to its parsed, normalized, validated, and compiled plan, never to a
query result or database value
([`datomic.query/query-cache`](../../src-clj/datomic/query.clj#L1077),
[`q*`](../../src-clj/datomic/query.clj#L1215)). Eviction recompiles against the
same supplied immutable database value. Entity maps also memoize attributes
only inside one entity object tied to a particular database snapshot.

Finally, connection and transaction response maps are bounded correlation
state, not durable storage. A Peer connection uses a 60-minute response map for
pending transaction/sync promises and removes entries on response or reconnect;
unanswered entries are bounded by write expiry. The acknowledged-publication
and transport gates exercise those
outcomes. Losing that bookkeeping may make a client outcome unavailable, but
it cannot undo or invent the PostgreSQL log descriptor that decides durability.

### Optional raw-value caches

Both recovered processes call `start-kv-cache`, but with the accepted
PostgreSQL configuration remote/local memcached properties are absent and
valcache lacks its required path/size pair, so the result is nil. No optional
cache is a hidden prerequisite. If configured, `cluster-stack` combines local
memcached, direct valcache, and remote memcached as repairable near/far value
stores with five-millisecond fallback, then places PostgreSQL at the final far
side ([source](../src-clj/datomic/cluster_stack.clj#L904)). A far-store hit can
repair a near miss; `reset-cache`/`skip-cache` bypass the near read, and
`skip-cache` suppresses repair
([`double-store`](../../src-clj/datomic/core2/val_store/double_store.clj#L27)).

The recovered implementations have bounded designs—memcached skips values over
one million bytes and can recreate an unhealthy client; direct valcache uses a
bounded deduplicating put pool, atomic temp-file replacement, polling/eviction,
and a one-million-byte value limit. They remain performance facilities with
their own outage and shutdown surfaces. Spy memcached and direct valcache are
coherent but unexercised; the alternate dynamically resolved Folsom client and
universal optional-cache failure behavior are partial and outside the supported
PostgreSQL core guarantee.

### Retained cache evidence

No new process run was warranted. The current Peer and Transactor
`datomic.cache` source hashes still match the input ledger of the focused gate
at `/tmp/datomic-stage2-cache-overlap-v3`. Its 214-entry manifest independently
verifies at SHA-256
`aa61b91b9ef75427cf887a674058ca6b4adb23dded576246837e7085f6318385`.
The freshly built, implementation-isolated recovered lanes validate all public
cache constructors; hit/miss and cached/uncached routing; transformer errors;
read-ahead disabled/hit/miss behavior; lookup population, falsey replacement,
remove, and clear; same-key in-flight collapse; failure cleanup/retry; and
repair-stack read/write/close ordering. Its original lanes were oracle-only;
they are not candidate dependencies or runtime fallback.

The accepted partition-v6 runtime supplies the integrated side. Both JVMs log
one 128-MiB `:cache/create` event and start with `ObjectCacheCount 0`. A's first
full interval then records 168 object-cache probes, 101 hits, and 29 resident
objects; promoted B independently records 41 probes, 15 hits, and 21 resident
objects while replaying 37,372 bytes. Both intervals have low/high 0/1, proving
that actual misses and hits occurred. Those processes still produce one
authoritative lineage and same-/fresh-Peer equality through basis 1066.

Index-v5 adds three fresh JVM caches, three successful Peer snapshots, positive
replay, explicit index adoption, and finally zero-replay loading at
`tail-t = index-t = 1066`; its 113-entry manifest verifies at
`c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238`.
Together the focused mechanics and integrated runs validate correctness with
empty and populated local caches, eviction-equivalent misses, index arrays
enabled, read-ahead enabled, process replacement, and Peer agreement. Exact
capacity/eviction timing, query-plan hit rate, the admin clear dispatcher, and
optional raw-cache behavior are not silently promoted to validated claims.

## Authority, failover, and fencing

Coordination ownership and per-database log ownership are distinct CAS layers.
The complete source walk and the in-flight, concurrent, and partition/heal
evidence are documented in
[Authority and HA source map](postgresql-vertical-slice.md#authority-and-ha-source-map).
In short, a successful `pod-coord` CAS selects the serving endpoint, while a
successful `pod-log-tail/<db-id>` descriptor CAS selects Peer-visible history.
Stale nodes lose their next heartbeat CAS and enter the shared critical-failure
shutdown path.

## Rolling optional-scope ledger

This table changes only when a subsystem slice supplies new evidence. A source
surface being present is not itself an implementation obligation.

| Facility encountered so far | Classification | Current boundary |
|---|---|---|
| PostgreSQL `:sql` configuration and service startup | validated | JDBC URL/driver/user/password, `select 1` validation, pool construction/test-on-borrow, memory/PID/data paths, and unencrypted startup are retained; readiness requires port plus Peer work. |
| PID ownership and critical startup cleanup | validated | Exact PID/start time/argv and bounded `SIGINT` cleanup are retained. |
| Bounded stop and fresh-directory restart/catchup | validated | Repeated SIGINT-only exits, positive log replay, exact snapshots, closed ports, and final PostgreSQL shutdown are retained; fault rows explicitly count zero sessions. |
| Persistent-index publication and fresh-process adoption | validated | Explicit index `t 1066` publication and zero-replay root adoption are retained. |
| In-JVM orderly drain and PID-file lifecycle | partial | Master/Database shutdown protocols are source-mapped and heartbeat-conflict self-fencing is observed; normal SIGINT handler execution, database-worker drain, and PID-file deletion are not claimed. |
| HA heartbeat interval and endpoint publication | validated | Normal, standby, promotion, conflict, and credential-partition paths are retained at the exercised 5000/8000-ms values; invalid-bound combinatorics remain unexercised. |
| Local metrics and process-event wiring | validated | Immediate and 60-second interval reports, default identity callback invocation, lifecycle/catchup/index events, and the conflict-to-final-metrics self-fence chain are retained. |
| External monitoring callbacks and CloudWatch | partial | Callback construction and failure accounting are source-mapped; custom, per-stat, and CloudWatch delivery are `NOT_RUN`. |
| Internal `datomic.process.events` garbage-mark bus | coherent but unexercised | Synchronous keyed publish/subscribe and its log/index garbage publishers are source-mapped; direct delivery/effects await bounded maintenance. |
| Ping endpoint and S3 log rotation | coherent but unexercised | Conditional startup, health-path construction, five-minute directory watch, and critical-failure upload hooks are source-visible; no endpoint or bucket is exercised. |
| Local object, index-array, query-plan, and correlation caches | validated | Current-source cache mechanics, real PostgreSQL-path hits/misses, fresh-process reconstruction, and unchanged Peer semantics are retained; performance/capacity equivalence is not claimed. |
| Peer `release-object-cache` administration | coherent but unexercised | The action clears only the process-global immutable-object cache and cannot mutate PostgreSQL refs; direct dispatch is reserved for bounded maintenance. |
| Transport encryption/TLS | partial | Normalization/defaulting exists, but accepted gates force `encrypt-channel=false`; candidate TLS identity/trust provisioning and encrypted transport are `NOT_RUN`. |
| Spy memcached and direct valcache | coherent but unexercised | Optional immutable raw-value near stores, far-store fallback/repair, bounded values/queues, metrics, and cleanup hooks are mapped; accepted PostgreSQL runs configure neither. |
| Alternate Folsom cache and universal optional-cache outage behavior | partial | Dynamic dispatch and fallback boundaries are visible, but no implementation-wide startup/outage/shutdown guarantee is made. |
| REST startup | coherent but unexercised | Optional `rest-port` branch is not part of the supported PostgreSQL core. |
| DynamoDB/S3, Cassandra, Couchbase, Infinispan, dev/H2 stores | out of scope | Configuration dispatch is recovered, but Goal 4's authoritative backend is PostgreSQL. |
| Cloud credentials/configuration | partial | Relevant configuration and targeted redaction paths exist, but they are not a universal log sanitizer and no cloud service claim is made. |
