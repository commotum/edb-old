# Goal 7 transaction service contract

## Recovered behavior and native mapping

The docs define one serialized transaction queue and durable acknowledgement.
A timeout is not a rejection: the caller does not know whether the transaction
committed and must inspect recent history or retry with application knowledge.
Peer reads remain valid during transactor failover, and cross-peer causality is
established by synchronizing to a reported basis.

Recovered `peer.clj` separates `ConnectionState`, a 128-slot
`unsent-updates-queue`, a request-id-to-promise pending map, `await-tx-result`,
database notification, and an optional transaction report queue. Reconnect
clears unsent work and completes pending promises as unavailable. The native
mapping is a `TransactionService` handle plus one worker owning
`PostgresStore`, a bounded `sync_channel`, one response channel per admitted
request, and explicit report subscriptions. Dropping a caller never cancels an
admitted write. No recovered broker or wire protocol is required for these
semantics.

Recovered transactor lifecycle starts its transaction master only after the
storage heartbeat makes the process active, and tears the master down when
ownership fails. Native ownership is stronger: PostgreSQL stores a holder,
server-time lease, and monotonically increasing epoch. Every head publication
checks holder, epoch, and unexpired server-time lease inside the same SQL
transaction. Heartbeats determine takeover latency; the fence determines
safety.

## Request lifecycle and outcomes

1. Admission validates a nonempty request key and attempts a nonblocking send
   to the configured bounded queue.
2. A full queue returns `Busy/service-queue-full`; a stopped or nonleader
   service returns `Unavailable` without admitting work.
3. The single worker renews leadership as necessary, dequeues one request,
   and calls the existing durable transaction path with the held epoch.
4. Success is returned only after PostgreSQL commit. The report contains the
   immutable `db-before`, `db-after`, transaction datoms, tempids, basis, and
   replay flag.
5. Semantic rejection retains its stable category/code. Leadership loss is
   `Unavailable/service-leadership-lost`. A queue wait timeout is `Busy`; a
   caller response timeout or connection loss after admission is
   `UnknownOutcome` and includes the request key.
6. The service never automatically retries a semantic transaction. Repeating
   the same request key and exact request resolves the durable outcome;
   changing the request under that key is `Conflict`.

On graceful shutdown, the worker finishes the current PostgreSQL operation,
releases leadership conditionally, and completes queued work as unavailable.
On process death, response channels vanish; admitted callers have unknown
outcome and reconcile through the durable key. There is no unbounded pending
map.

## Reports and synchronization

Each successful commit or durable idempotent replay produces a report for its
requester. Live report subscribers receive newly processed non-replay commits
from all submitters while subscribed. Subscriptions are bounded: a slow
subscriber drops notifications and increments a counter rather than applying
backpressure to durability. Unsubscribe/drop removes it. This deliberately
changes the recovered unbounded `LinkedBlockingQueue` to prevent the documented
memory-accumulation hazard while preserving the opt-in live-report intent.

Peers continue to return their last immutable database during writer outage.
After a service response communicates basis `t`, another peer uses its existing
`sync(t)` behavior to establish the causal order. Failover does not invalidate
old `Arc<Database>` snapshots.

## Lease and fence invariants

- Epochs are positive and strictly increase on every acquisition after expiry
  or conditional release. They never reset or derive from a process clock.
- Only the current holder/epoch can renew or release. Renewal does not change
  epoch.
- Acquisition and renewal calculate expiry from PostgreSQL
  `clock_timestamp()`. A candidate cannot acquire an unexpired lease.
- A transaction locks the lease and database head, verifies the exact holder
  and epoch and that expiry is still in the future, then assesses and publishes
  in that same SQL transaction. A paused old process is fenced after takeover
  even if its cached database and TCP connection remain usable.
- Lease loss can reject work; it can never authorize two publications or fork
  the immutable predecessor-hash chain.

## Explicit scope

The service is an in-process Rust boundary suitable for embedding behind a
future transport. Authentication, TLS termination, HTTP/gRPC, endpoint
discovery, Kubernetes integration, backup, GC, excision, and fleet telemetry
are not needed to prove serialized availability and belong outside this goal.
