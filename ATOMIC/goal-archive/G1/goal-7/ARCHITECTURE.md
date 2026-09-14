# Goal 7 architecture and deviations

## Final shape

`TransactionClient` is a cheap cloneable admission handle. It sends owned
`TransactionRequest` values into one bounded `sync_channel`; each admitted
request owns a capacity-one result channel represented by `TransactionTicket`.
`TransactionService` owns the receiving worker and its `PostgresStore`, so
transaction assessment and publication cannot run concurrently inside one
leader. `TransactionStandby` is a small acquisition loop that becomes a normal
service only after the current server-time lease expires.

PostgreSQL is both durable history and coordinator. One
`atomic_transactor_leases` row per scope contains holder, epoch and expiry.
Acquisition is advisory-serialized to cover the absent-row race, renewal and
release match holder/epoch, and `transact_fenced` locks and verifies the lease
inside the same SQL transaction as request lookup, immutable log insertion,
and head CAS. This preserves the recovered active-master lifetime boundary but
makes stale-owner rejection independently testable.

## Recovered mappings

- `ConnectionState` plus `unsent-updates-queue` becomes `TransactionClient`
  plus bounded `sync_channel`.
- Pending request promises and `await-tx-result` become `TransactionTicket` and
  `recv_timeout`; timeout is explicitly unknown outcome, not cancellation.
- The single transactor master becomes one store-owning worker.
- Storage heartbeat active/standby becomes server-time epoch lease plus
  `TransactionStandby` polling. The documented two-missed-heartbeat policy is a
  deployment tuning choice; correctness does not depend on its exact interval.
- `notify-data` and the optional report queue become immutable
  `ServiceTransactionReport` values and bounded subscriptions.
- Reconnector failure of pending work maps to deterministic unavailable
  completion during leadership loss/shutdown.

## Deliberate deviations

Atomic does not reproduce HornetQ/Artemis, endpoint publication, soft response
maps, JVM futures, or a network protocol. They are transport/runtime machinery,
not database semantics. The service is transport-neutral Rust suitable for
embedding behind a later authenticated protocol.

The recovered peer report queue is unbounded and relies on applications to
drain/remove it. Native subscriptions are bounded and nonblocking: a slow
subscriber drops reports and records the count, never delaying a durable
commit or consuming memory without limit. Requesters always retain their own
dedicated result ticket.

The default recovered peer queue has 128 entries; native capacity is explicit
configuration so resource budgeting is visible. Metrics count accepted work
until the worker accounts for receipt, so high-water can be queue capacity plus
the single worker handoff; the channel itself never stores beyond capacity.

## Measured evidence

The focused stress run uses twelve concurrent producers, four queued slots,
and twenty sequential transactions per independent database. It committed all
240 transactions in about 1.7 seconds on the temporary local PostgreSQL 15.11
instance, observed queue/handoff high-water five, exercised `Busy` admission,
and recovered every database at contiguous basis 20 with final value 20. This
is correctness/capacity evidence, not a general throughput benchmark.
