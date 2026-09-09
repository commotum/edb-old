# Goal 7 Continuation Prompt

```text
Continue Goal 7, availability and the fenced production transaction service,
using /home/jake/Developer/atomic/goal-7/0-plan.md and
/home/jake/Developer/atomic/goal-7/0-loop.md.

Treat datomic_pro_docs as semantic authority, completed Goal 1–6 behavior as
the executable lower boundary, and 1.0.7705 as the default blueprint for the
serialized transactor, ConnectionState, bounded queues, pending promises,
transaction reports, reconnection, anomalies, heartbeat and standby control
flow. Use Rust and PostgreSQL only. Translate those boundaries idiomatically;
do not recreate JVM messaging or introduce a generic coordinator/storage layer.

Reconcile the plan with artifacts and real PostgreSQL evidence, then advance
the first unfinished stage. Preserve a small deterministic write owner,
bounded admission, durable idempotency, explicit unknown outcomes, actionable
errors, monotonic peer reads, and manual application retry. Leadership must use
server-time leases with monotonic epochs, and every database-head publication
must be fenced by the held epoch in the same PostgreSQL transaction. Verify
with concurrency, overload, restart, pause/process-death, acknowledgement-loss,
and stale-owner tests. Keep the service transport-neutral; network/auth and
Goal 8 operations are out of scope.

Continue until stale owners cannot publish or fork history, accepted and
pending requests settle truthfully, failover restores writes, peer reads remain
available, and the full integrated suite passes. Record significant decisions,
limits and evidence in the plan; report only a concrete blocker or uncertainty.
```
