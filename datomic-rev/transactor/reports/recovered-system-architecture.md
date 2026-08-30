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

## Core state model and transaction spine

### Immutable content behind mutable selectors

The supported backend uses one deliberately small PostgreSQL relation:

```sql
CREATE TABLE datomic_kvs (
  id text PRIMARY KEY,
  rev integer,
  map text,
  val bytea
)
```

The executing gate creates that exact shape
([runner](../scripts/validate-postgresql-vertical-slice.sh#L2670)). Its apparent
simplicity hides a crucial distinction:

| Stored role | Mutation rule | What it can decide |
|---|---|---|
| UUID-addressed value | create once, then read by ID | immutable log, index, directory, segment, or metadata content |
| revisioned reference/pod | update only at the expected old revision | which immutable content is authoritative now |
| `pod-coord` | expected-revision CAS | active transport endpoint |
| `pod-log-tail/<db-id>` | expected-revision CAS plus prior-tail etag | authoritative transaction lineage |
| `ref-index-root/<db-id>` | expected-revision CAS | persistent index root; it may lag the log |

The SQL layer makes the selector rule literal: reference updates are
`UPDATE datomic_kvs ... WHERE id=? AND rev=?`
([`datomic.sql/update`](../src-clj/datomic/sql.clj#L136)). A one-row update wins;
a zero-row update conflicts. UUID values can be physically durable without any
winning selector pointing to them. Such a value is storage, but it is not
database history.

Peer `Database` objects follow the same split. Each is an immutable value with
a transaction `basisT`, a possibly older persistent `indexBasisT`, and the
memory-log delta needed to answer reads through `basisT`. Replacing a Peer
database reference does not mutate an old database value. Persistent indexing
can therefore lag ordinary transactions without changing which transactions
are durable, and cache eviction can discard representations without changing
any selector.

### Submission and transport admission

One transaction starts as follows:

```text
application
  -> d/transact-async
  -> Peer UUID correlation + pending Future
  -> 128-slot local outgoing queue
  -> Fressian message to <db-id>.tx-submit
  -> Transactor five-slot unprocessed queue
  -> single per-database processor
```

`d/transact-async` delegates to the connection
([API](../../src-clj/datomic/api.clj#L102)). The Peer creates only a UUID,
transaction data, and optional options at this stage
([`create-procargs`](../../src-clj/datomic/transaction.clj#L271)); semantic
transaction validation is not performed eagerly. The connection creates a
settable Future, records it in the UUID-keyed response map, and puts the request
on its outgoing queue
([`Connection.transactAsync`](../../src-clj/datomic/peer.clj#L522)). The queue
has 128 slots and the response map expires unanswered correlations after 60
minutes ([connection construction](../../src-clj/datomic/peer.clj#L899)).
Because the recovered `queue/put` is blocking, the word "async" describes the
returned Future, not an unconditional guarantee that the API call itself can
never backpressure at a full queue.

The connector Fressian-encodes the procargs and sends them to the database
submission address
([`start-updater`](../../src-clj/datomic/connector.clj#L517)). A local encoding
error is delivered to the matching Future; a send/session failure enters the
connection-failure path. The Peer has no durable request ledger behind this
queue.

The active database Master creates a temporary Artemis consumer on the same
address. Its reader decodes a message, attaches optional prefetch state, puts a
transaction into the bounded FIFO, and only then acknowledges the Artemis
message ([`reader`](../src-clj/datomic/update.clj#L549)). There are five slots
and one transaction processor for this database
([database construction](../src-clj/datomic/update.clj#L2754)). Artemis
persistence is disabled on the recovered server
([source](../src-clj/datomic/artemis_server.clj#L97)). This acknowledgement
therefore means only "admitted to this Transactor's memory." It precedes
transaction validation, log encoding, PostgreSQL publication, and client
acknowledgement.

### Novelty, ordering, and speculative local state

The single processor prefers internal priority work and otherwise takes the
next submitted transaction. `process-transaction` applies `db/with-tx` to the
current database reference
([processor](../src-clj/datomic/update.clj#L1090)). `db/with-tx` expands entity
maps, list forms, and transaction functions; resolves tempids; validates the
result; applies cardinality, uniqueness, CAS, and attribute predicates; and
returns the actual added/retracted datoms plus the next immutable database
value ([source](../src-clj/datomic/db.clj#L7813)). The returned datoms are the
novel effect, not merely a copy of the submitted forms.

The processor installs `:db-after` in its local atom before durable logging.
That makes dequeue/current-database order the Transactor's speculative order,
but not yet PostgreSQL truth. An accepted item carries its UUID, datoms, `t`,
tempids, I/O statistics, and a new unresolved `:logged` promise. A rejected
item carries a serialized error and a pre-realized `:logged` promise because
there is deliberately no log append. Rejection leaves the published descriptor
and visible basis unchanged, although transaction-number allocation is only
monotonic: rejected attempts can leave gaps.

Installing local state before storage is safe only because log failure is
process-fatal. The writer raises `UnableToWriteLog`, realizes the shared
critical-failure state, closes serving transport through its registered
handler, and exits rather than continuing from an in-memory database that
PostgreSQL did not publish
([writer failure](../src-clj/datomic/update.clj#L1699)). Restart reconstructs
from the descriptor-referenced lineage, never from that dead process's atom.

### Publication and acknowledgement ordering

Accepted processing forks into two preparation lanes joined by the same
`:logged` promise:

```text
                         +-> encode transaction log -> writer batch --+
accepted processor item |                                          |
                         +-> encode Peer result ----------------+    |
                                                                |    v
UUID immutable tail candidate -> pod-log-tail expected-rev CAS -+-> logged!
                                                                     |
                                                                     v
                                                        send Peer result
```

The log `fressianer` encodes accepted datoms and queues them to the writer
([source](../src-clj/datomic/update.clj#L1346)). In parallel, the notification
fressianer can encode the result, but it preserves the unresolved promise
([source](../src-clj/datomic/update.clj#L1473)). The writer may batch adjacent
accepted transactions in FIFO order and calls `LogImpl.append`
([source](../src-clj/datomic/update.clj#L1645)).

`LogImpl.append` extends only its candidate tail, increments the descriptor
revision, and calls `write-tail-descriptor`; a conflict throws instead of
returning a new log
([append](../src-clj/datomic/log.clj#L1183)). The storage adapter first creates
a random immutable tail node whose metadata links to the prior etag, then tries
the revision-guarded pod update
([candidate](../src-clj/datomic/kv_cluster.clj#L246),
[`pod update`](../src-clj/datomic/kv_cluster.clj#L320)). PostgreSQL performs the
UUID insert and the selector update through the recovered SQL layer. On an
ambiguous write, read-back accepts only the exact attempted revision and tail;
anything else is a conflict
([reconciliation](../src-clj/datomic/kv_cluster.clj#L409)).

The successful expected-revision CAS of `pod-log-tail/<db-id>` is the
authoritative transaction publication boundary. It is the first state from
which a fresh process can recover the transaction. An immutable tail node
inserted before a losing or interrupted CAS is an unreachable candidate, not a
transaction that a standby can discover and adopt.

Only after `log/append` returns does the writer realize each transaction's
`:logged` promise. `block-notifier` waits up to 60 seconds for that promise
before sending the already encoded result to `<db-id>.tx-result`
([notification gate](../src-clj/datomic/update.clj#L1775)). Normal success is
therefore ordered after publication. Log treeification happens later: it moves
older tail content into immutable leaf/directory/root values and adopts the new
root with another descriptor CAS while retaining newer tail entries. That is
representation compaction, not a second transaction commit.

The acknowledgement edge is best read as a cut table:

| Fault cut | Durable state | Honest client interpretation |
|---|---|---|
| before descriptor CAS | an immutable candidate may exist, but the old descriptor remains authoritative | no success; after failure the original Future may be unavailable and recovery should find the effect absent |
| after descriptor CAS, before result delivery | the transaction is in the authoritative lineage | the outcome may be a report or unavailable; unavailable does not mean absent |
| after Peer receives the result | the transaction is durable and this Peer has advanced its database value before completing the Future | the returned report contains matching `db-before`, `db-after`, datoms, and tempids |
| competing/stale writer loses descriptor CAS | its candidate is not authoritative | writer failure triggers process self-fencing; it must not notify success |

### Peer visibility, reconnect, and retry responsibility

The Peer notifier dispatches result types by their decoded transaction message
shape ([dispatch](../../src-clj/datomic/connector.clj#L283)). For a successful
transaction, `notify-data` removes the UUID correlation, applies only the
previously unseen suffix of datoms to the connection's database atom, builds
the report, completes the originating Future, optionally enqueues the report,
and releases `basisT` waiters
([notification](../../src-clj/datomic/peer.clj#L414),
[`accept-new-data`](../../src-clj/datomic/peer.clj#L239)). Repeated or
overlapping notifications can therefore advance local state idempotently. An
error removes the same pending correlation and delivers the deserialized
exception without advancing the database
([source](../../src-clj/datomic/peer.clj#L400)).

Connection failure has intentionally conservative semantics. Every reconnect
attempt clears unsent requests and completes all still-pending transaction
Futures with `:cognitect.anomalies/unavailable`, then rereads coordination and
rebuilds transport/database state
([`fail-pending-txes`](../../src-clj/datomic/peer.clj#L277),
[`create-connection`](../../src-clj/datomic/peer.clj#L899)). The recovered Peer
does not automatically resubmit. An application that receives unavailable
must sync or read current durable state and use its own logical identity,
precondition, or idempotency rule to decide whether anything remains to be
submitted. Blind retry of an arbitrary non-idempotent transaction is not an
exactly-once guarantee.

`sync(conn,t)` waits locally for `basisT >= t`; `sync-index(conn,t)` waits for
`indexBasisT >= t`. Zero-argument `sync` is different: it sends a UUID-tagged
sync message and is the documented boundary for transactions already complete
when the call was made. It is not a promise to wait for an arbitrary in-flight
transaction or persistent-index construction
([watchers](../../src-clj/datomic/peer.clj#L214),
[`Connection.sync`](../../src-clj/datomic/peer.clj#L559)).

### Indexing, restart, and HA composition

Transaction publication, persistent indexing, and serving authority are three
ordered but separate selectors:

```text
pod-log-tail CAS       -> transaction is durable and recoverable
ref-index-root CAS     -> a persistent index covers a later basis
pod-coord CAS          -> a process endpoint may serve
```

A Peer answers through its persistent root plus the memory-log delta. Explicit
indexing later writes EAVT, AVET, AEVT, and reverse-reference structures and
publishes the new root by CAS; Peer adoption releases `indexBasisT` waiters.
On restart, a fresh Transactor loads the current index selector, reads the
current log descriptor, and replays only descriptor-referenced transactions
after the index basis. Neither restart nor a promoted standby scans for
unreferenced immutable candidates.

HA adds a coordination CAS without weakening the log rule. `pod-coord` selects
the endpoint, but a newly active B must also claim the database log descriptor
and catch up its referenced lineage. If A already won a transaction descriptor
CAS, B catches up that transaction. If A only created an immutable candidate
and B claims the unchanged descriptor first, the candidate remains absent.
When a healed stale A next loses either authority assumption, its heartbeat or
log CAS enters critical failure and self-fences. The fuller lifecycle source
map is in [Authority and HA](#authority-failover-and-fencing).

### Retained evidence for the spine

All ten manifests below were reverified from their run roots during this
reconstruction. Every run records `status=passed`, `last.step=complete`, and
`services.running=false`.

| Boundary | Retained run and manifest-file SHA-256 | Exact contribution |
|---|---|---|
| normal log/restart | `/tmp/datomic-recovered-pair-live-v3` (108 entries), `100245c7dbb8639ea4bbc8ad46594818bfb9909e6aae75a3b79c1b26263d24bf` | basis 1001/1066 commits, positive 37,372/64,032-byte catchup, equal fresh-process snapshots |
| rejection and order | `/tmp/datomic-recovered-pair-transaction-boundaries-v5` (91), `8efea20819c62f167d774d29d7a330f6ef1e018884c50f16a533376d667a3bdd` | stale-CAS/uniqueness rejection without root change, two one-winner CAS rounds, four accepted transactions in one report/basis/root order |
| prepublication crash | `/tmp/datomic-recovered-pair-ack-crash-v2` (110), `6f3a5ab8d6c877d7a9f2f8e2e23c4670b9a2228995dd290f854426a5a20fd0c5` | immutable append candidate exists while descriptor CAS is blocked; kill/rollback leaves the descriptor and semantic database unchanged |
| postpublication/pre-result crash | `/tmp/datomic-recovered-pair-ack-postpublication-v4` (106), `d8ac5e5314603c1bca54aa8a773d60a7eec136e29066b4d220d110e1591374bf` | descriptor advances before result; restart preserves exactly one effect and this deliberately buffered original Future later returns its report |
| same-Peer reconnect | `/tmp/datomic-recovered-pair-transport-v2` (123), `87eaf2e129aa5d3ec12b4e84a9c3143e623b21767a00ee0c3916ea3c037383f6` | unavailable during a bounded pause, same-connection recovery/write at basis 1099, fresh-Peer agreement |
| in-flight HA | `/tmp/datomic-recovered-pair-ha-inflight-v8` (101), `5243885f1c0c85dbe2967171856258ad7f7665fd38391bd72444b4c72c1a2887` | A publishes first; B catches that referenced transaction; original Future is unavailable; same/fresh Peers agree and stale A fences |
| concurrent HA, A wins | `/tmp/datomic-recovered-pair-ha-concurrent-v6` (101), `773dd1f4c7e6c78f1a55d4f2742f6b4852008c8f2236da2ce2aa31ea27948d47` | four published effects found, zero resubmitted, one order through basis 1009; all original Futures unavailable |
| concurrent HA, B wins | `/tmp/datomic-recovered-pair-ha-concurrent-v7` (102), `e3866a7611b8ac8a6132f8be12d0a5cc4f4391226911684b98bb6d432f4e4798` | A's candidate is unreachable; the external probe finds zero effects and explicitly resubmits four logical intents, ending in the same order |
| credential-scoped partition/heal | `/tmp/datomic-recovered-pair-ha-partition-v6` (126), `4e0a4ca8c8c58863712f8252dd66e320756dd70a02a38288f320ecf924299272` | B promotes while A is live/storage-incapable; heal produces stale-A conflict/fence; same/fresh Peers and one log lineage agree |
| persistent index | `/tmp/datomic-recovered-pair-index-v5` (113), `c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238` | queued request plus `sync-index`, SQL growth, root adoption, zero-replay fresh restart at index/log basis 1066 |

The postpublication cut demonstrates one schedule in which a buffered original
Future survives; the in-flight/concurrent HA cuts demonstrate schedules in
which original Futures become unavailable. Neither outcome is universal. The
supported invariant is the descriptor-referenced transaction order, not
transparent Future survival. Likewise, concurrent v7's explicit
identifier-based probe performs the resubmission; Peer internals do not. The
accepted partition result is v6. Partition v4 remains an immutable failed
diagnostic because its evidence-secret gate failed even though its behavioral
observations were green.

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

## Bounded maintenance and reclamation

### Three different completion contracts

The public maintenance surfaces do not share one meaning of "done." Keeping
their acknowledgement boundaries distinct prevents a queue reply or a local
cache operation from being mistaken for durable PostgreSQL maintenance.

| Operation | Immediate result | Actual completion boundary | Authority effect |
|---|---|---|---|
| `Peer.administerSystem {:action :release-object-cache}` | synchronous `:completed` | the process-global object cache has been cleared | none; no Transactor RPC or PostgreSQL reference write |
| `Connection.requestIndex` | `true` after a synchronous admin RPC replies that the request was queued | `sync-index` reaches the requested basis after root publication and Peer adoption | expected-revision CAS of the database's index-root reference |
| `Connection.gcStorage` | `nil` after a synchronous queue-ack RPC when a connector is present | no client completion future; later garbage logs/SQL effects must be observed independently | deletion of previously marked immutable values older than the supplied cutoff |

The first operation is wholly local. The recovered
[`administer-system`](../../src-clj/datomic/peer.clj#L1341) validates the action,
clears `domain/system-cache`, and returns `:completed`. It does not clear query
plans, connection state, the Peer memory index, or any PostgreSQL reference.
The cache-overlap gate validates the underlying clear and miss/repopulation
mechanics, while the PostgreSQL cache evidence above validates the ordinary
refetch path. The thin public dispatcher itself is source-coherent but was not
given a fresh end-to-end run.

Index maintenance has a two-phase client contract:

```text
request-index
  -> Peer admin RPC
  -> Transactor replies {:queued db-id}
  -> priority queue records the requested basis
  -> one index semaphore prepares the memory index
  -> segment the current log tree
  -> write a new immutable four-family index
  -> expected-revision CAS publishes the new index root
  -> Transactor adopts it and notifies Peers
  -> sync-index completes after this Peer observes indexBasisT >= requested t
```

The Peer sends the request and returns `true` only after the admin reply
([`requestIndex`](../../src-clj/datomic/peer.clj#L576)); the Master reply is
still only `{:queued ...}` ([`run-admin-command`](../src-clj/datomic/update.clj#L3199)).
The processing state machine records the target, serializes indexing, ensures
the log tree, and starts the immutable build
([`process-request-index`](../src-clj/datomic/update.clj#L791)). Publication is
the index-reference CAS; only after it succeeds does the publisher report
superseded keys to the garbage bus
([`merge-db`](../src-clj/datomic/index.clj#L5980)). The Transactor then completes
local adoption and notifies the Peer
([`process-new-index`](../src-clj/datomic/update.clj#L685)). Thus a queued
request can be lost by a crash before publication, while an already published
root remains durable; callers that need completion re-request safely and wait
with `sync-index`.

This path is directly retained at `/tmp/datomic-recovered-pair-index-v5`.
The recovered Peer records request acceptance at basis 1066 and waits until
`sync-index` reports 1066. PostgreSQL grows from 42 rows/18,993 value bytes
before publication to 86 rows/34,597 bytes afterward. A third fresh
Transactor then loads `tail-t = index-t = 1066` with zero replay and produces
the same Peer snapshot fingerprint. The run is `PASS`; all 113 manifest
entries reverify, and the manifest-file SHA-256 is
`c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238`.
This validates explicit index maintenance, including its durable completion
boundary, without claiming automatic threshold scheduling or interrupted-build
recovery.

### Garbage marking and collection

Garbage collection is deliberately separated from live-root selection:

```text
successful log/index replacement identifies superseded immutable UUIDs
  -> synchronous datomic.process.events publication
  -> startup-installed handler queues timestamped marks on garbage-agent
  -> more than 1,000 pending keys (or an explicit tool flush)
       writes immutable leaf/directory/root nodes
       and CAS-publishes ref-gc-root/<db-id>
  -> gc-storage queues collection-agent with an operator cutoff
  -> delete only marked batches whose timestamp is strictly before the cutoff
  -> log collected count / GarbageDeletedCount, or alarm StorageGCFailed
```

`run*` installs the keyed subscriber before storage startup
([source](../src-clj/datomic/transactor.clj#L723)). Log and index publication
emit `:datomic.garbage/mark`; the in-process bus invokes subscribers
synchronously, but the installed handler hands the work to an agent
([bus](../src-clj/datomic/process/events.clj#L13),
[`handler`](../src-clj/datomic/garbage.clj#L370)). The default marker persists a
leaf only when its accumulated size becomes strictly greater than 1,000; the
explicit `flush-garbage` path is used by repair/rebuild tools, not by the normal
Transactor shutdown path
([marking](../src-clj/datomic/garbage.clj#L242),
[`flush`](../src-clj/datomic/garbage.clj#L398)). Small pending batches can
therefore remain process-local, and a mark-write failure can leak unreachable
storage without making a retired value live again.

The durable ledger is itself an immutable tree selected by the separate
`ref-gc-root/<db-id>` CAS
([`ensure-root-ref`](../src-clj/datomic/garbage.clj#L104),
[`append-leaf`](../src-clj/datomic/garbage.clj#L148)). Collection traverses
only that ledger and deletes entries with `mark timestamp < older-than`; it
does not derive reachability by walking live roots
([`gc-leaf`](../src-clj/datomic/garbage.clj#L472),
[`gc`](../src-clj/datomic/garbage.clj#L550)). The public Peer call is
fire-and-forget and even becomes a silent no-op if no connector is present
([`gcStorage`](../../src-clj/datomic/peer.clj#L504)). The Master acknowledges
queueing, while the process-global collection agent converts later failures
into an alarm and log record rather than a client exception
([admin](../src-clj/datomic/update.clj#L3216),
[`queue-gc`](../src-clj/datomic/garbage.clj#L610)). A successful API return is
therefore not proof of reclamation.

Backup/restore has a separate, narrower evidence boundary. The isolated
recovered-Peer gate at `/tmp/datomic-stage2-adversarial-final-v2` validates
full and incremental backup, failed-root retry, missing/corrupt rejection,
interrupted-restore retry, exact `t1`/`t2`/`t3` state, and writable restored
databases. All 118 manifest entries reverify at manifest SHA-256
`25e5f821a84bbde27cb85a985a2728cffbe73509dfd2f7b70c115ed6ca2cf721`.
That lane used a test-only `stage2-file:` adapter and a licensed Transactor as
an isolated external fixture; its candidate classpath records no original Peer,
core2, or Transactor classes. It validates the recovered backup engine under
that fixture, not production file/S3 adapters or backup/restore of the complete
recovered Peer/Transactor pair.

No fresh maintenance process was started for this slice. A cache-clear-only
run would exercise a thin dispatcher whose clear/refetch mechanics are already
validated, while a meaningful garbage run would need to create and account
for more than 1,000 exact retired IDs, wait for asynchronous marking and
collection, and prove live-root preservation around destructive deletes. A
small-catalog zero-count run can even create an empty garbage root and would
not validate reclamation. That machinery would turn a bounded operational
slice into a retention campaign. The honest boundary is: explicit persistent
index maintenance is validated; local cache-release dispatch and garbage-tree
construction are coherent but directly unexercised; public asynchronous
garbage collection is partial. Deleted-database collection, excision,
full-text reclamation, and recovered-pair backup/restore are not implied by
this result.

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
| Internal `datomic.process.events` garbage-mark bus and durable mark tree | coherent but unexercised | Synchronous keyed publication, async marking, the greater-than-1,000 persistence threshold, and the `ref-gc-root/<db-id>` CAS tree are source-mapped; no retained run directly proves durable mark delivery. |
| Ping endpoint and S3 log rotation | coherent but unexercised | Conditional startup, health-path construction, five-minute directory watch, and critical-failure upload hooks are source-visible; no endpoint or bucket is exercised. |
| Local object, index-array, query-plan, and correlation caches | validated | Current-source cache mechanics, real PostgreSQL-path hits/misses, fresh-process reconstruction, and unchanged Peer semantics are retained; performance/capacity equivalence is not claimed. |
| Peer `release-object-cache` administration | coherent but unexercised | Underlying clear/miss/repopulation mechanics are validated; the thin synchronous action clears only the process-global immutable-object cache and cannot mutate PostgreSQL refs, but direct public dispatch is `NOT_RUN`. |
| Explicit `request-index` maintenance | validated | Queue acknowledgement is distinguished from completion; retained `sync-index`, PostgreSQL growth, root publication/adoption, zero-replay fresh-process loading, and equal Peer state close the exercised path. |
| Active-database `gc-storage` | partial | Mark/cutoff/delete and alarm paths are source-coherent, but the API has no completion future, sub-threshold marks may remain in memory, and no destructive retained run proves exact retired-ID deletion with live-root preservation. |
| Backup engine and recovered-pair production adapters | partial | The recovered [`datomic.backup`](../../src-clj/datomic/backup.clj#L1444) engine is validated in an isolated recovered-Peer lane, but that lane used a test-only `stage2-file:` adapter and a licensed Transactor fixture. The recovered [filesystem](../src-clj/datomic/fsbackup.clj#L96), [S3](../src-clj/datomic/s3backup.clj#L50), and [CLI](../src-clj/datomic/backup_cli.clj#L59) surfaces are source-coherent; production-adapter and complete recovered-pair backup/restore behavior is `NOT_RUN`. |
| Full-text indexing, search, and reclamation | coherent but unexercised | Active/history construction, Lucene mutation, publication, and garbage-key wiring are source-connected through [`datomic.fulltext`](../src-clj/datomic/fulltext.clj#L580) and the [persistent merge](../src-clj/datomic/index.clj#L4956), but index-v5 used no nonempty full-text attribute workload. |
| Excision | coherent but unexercised | Target expansion, time bounds, reference traversal, removal predicates, and persistent-merge integration are source-visible in [`datomic.excise`](../src-clj/datomic/excise.clj#L37); no retained recovered-pair run submits `:db/excise`. |
| Deleted-database garbage collection | partial | Catalog marking and batched log/index/ref teardown are source-coherent in [`gc-deleted-db`](../src-clj/datomic/garbage.clj#L672), but exact deletion, restored-ID preservation, interruption/retry, and catalog cleanup are `NOT_RUN`. |
| Transport encryption/TLS | partial | Normalization/defaulting exists, but accepted gates force `encrypt-channel=false`; candidate TLS identity/trust provisioning and encrypted transport are `NOT_RUN`. |
| Spy memcached and direct valcache | coherent but unexercised | Optional immutable raw-value near stores, far-store fallback/repair, bounded values/queues, metrics, and cleanup hooks are mapped; accepted PostgreSQL runs configure neither. |
| Alternate Folsom cache and universal optional-cache outage behavior | partial | Dynamic dispatch and fallback boundaries are visible, but no implementation-wide startup/outage/shutdown guarantee is made. |
| Recovered Peer Server | coherent but unexercised | Connection/catalog caches, Client SPI delegation, HMAC authentication, Transit codecs, bounded Nano HTTPS serving, and `/health` are source-connected ([server construction](../src-clj/datomic/peer_server.clj#L417)); no retained request, authentication, TLS, failure, or shutdown run exists. |
| Recovered `datomic.peer-client` adapter | coherent but unexercised | This is an in-process `datomic.client.api` adapter over recovered embedded Peer operations ([client/connection adapters](../src-clj/datomic/peer_client.clj#L161)), not evidence for a network thin Client; it has no retained behavioral gate. |
| Network thin Client | out of scope | Its implementation remains in hash-pinned ordinary `client*.jar` dependencies ([dependency ledger](stage-1-candidate-dependencies.tsv#L377)); it was neither reconstructed nor exercised end to end with Peer Server. |
| REST startup | coherent but unexercised | Transactions, queries, reads, events, middleware, routes, and Jetty startup are source-connected in [`datomic.rest`](../src-clj/datomic/rest.clj#L966), but the optional [`rest-port`/`rest-alias` branch](../src-clj/datomic/transactor.clj#L973) is not exercised or included in the supported PostgreSQL core. |
| Presto integration | out of scope | The repository inventories only the hash-bound licensed-original optional Presto JAR; no candidate source, launcher, or retained run joins the recovered runtime ([frozen boundary](stage-0-boundary.md#L111)). |
| Console | out of scope | The repository inventories only the hash-bound licensed-original optional Console JAR; no Console implementation or startup path was recovered or exercised ([frozen boundary](stage-0-boundary.md#L111)). |
| DynamoDB/S3, Cassandra, Couchbase, Infinispan, dev/H2 stores | out of scope | Configuration dispatch is recovered, but Goal 4's authoritative backend is PostgreSQL. |
| Cloud credentials/configuration | partial | Relevant configuration and targeted redaction paths exist, but they are not a universal log sanitizer and no cloud service claim is made. |

## Supported Goal 4 release boundary

As of 2026-08-29, the released study boundary is the recovered Peer plus the
complete recovered Transactor over PostgreSQL, using plaintext Artemis
transport. It includes configuration and readiness, transaction rejection and
ordering, log publication, acknowledgement fault cuts, Peer visibility and
unknown-outcome recovery, persistent-index publication/adoption, bounded
shutdown/restart, local monitoring and process events, cache semantics,
explicit index maintenance, and credential-scoped active/standby HA. It does
not include universal scheduler/topology equivalence or any facility marked
partial, coherent but unexercised, or out of scope above.

The runnable entry point remains:

```bash
transactor/scripts/validate-postgresql-vertical-slice.sh --help
```

The runner rebuilds and seals both recovered candidate sides before any
service starts; its focused modes make the smallest implicated boundary
repeatable. The accepted asymmetric HA result is
`/tmp/datomic-recovered-pair-ha-partition-v6`: `summary.properties` and
`run-status.properties` both pass, all 126 evidence entries verify, both
forbidden-runtime and forbidden-secret result files are empty, all three SQL
role session counts are zero after stop, and the final service-stop assertion
passes. Its candidate-origin probes report no original Peer, core2,
Transactor, Nano, key, or trust implementation artifacts. Partition v4 remains
an immutable failed diagnostic and is not promoted by this signoff.

The ten manifest-verified transaction-spine runs above remain the behavioral
release evidence. Current runner and root HA-probe inputs still match the
accepted v6 ledger at SHA-256
`3ea5147924ce78a067e9254c4723ba9b97ac55521cdb12f2ccf4d31f1577680c`,
`6bcdad60cb30aaa14c793273819cff2d190bfbfe087b01e67dc075bea4fc9867`,
and `23dbc79c8e0e416fa141188bb718def9d0b49ff825199f3bca10a13bc7e17b36`
for the runner, HA probe, and shared Peer workload respectively. Final
proportional checks exercised the runner's help entry point, shell syntax for
the runner and documented build/validation scripts, all local Markdown link
targets in the three handoff documents, evidence-manifest integrity/status,
and whitespace integrity. No service rerun was added: the working change is
explanatory only, the accepted executable inputs are unchanged, and the scope
reconciliation surfaced no contradiction in the supported PostgreSQL core.

This is a strongly coherent educational reconstruction, not a claim of
universal Datomic Pro equivalence or a redistributable replacement. Optional
extensions begin only from an explicit need or a concrete contradiction; their
mere source presence does not reopen this release boundary.
