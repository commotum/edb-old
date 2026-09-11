# Reacting to transactions

`Connection` listens for cross-process PostgreSQL commit/index wakeups. These
are empty, untrusted hints: only the authenticated log and native index opener
advance a connection. One process-wide bounded publisher coalesces hints;
publication never waits for notification SQL and failed/dropped hints do not
change transaction outcomes. Peers register before their first catch-up, repair
after reconnect, and check durable state every 30 seconds when otherwise quiet.
Use `ObservationConfig` with `Connection::connect_configured_with_observation`
to change that positive interval. Explicit synchronization APIs remain available.

`Connection::observation_sql_stats()` includes listener setup, reconnection and
authenticated catch-up driver work. Socket waiting issues no SQL. Process-wide
SQL stats also include the publisher under `PeerObservation`;
`notice_publisher_stats()` reports cumulative queued, coalesced, dropped, sent
and failed hint attempts. It does not prove another process received a notice.
`notice_listener_stats()` records received/coalesced hints and the largest
driver batch. The listener polls at most 64 protocol messages per batch and
retains only a wakeup bit, avoiding the synchronous driver's unbounded notice
queue; forged payloads cannot supply a transaction or change a checkpoint.

## Bounded replay and live consumption

`ChangeConsumer` is separate from the existing lossless, unbounded transaction
report queue. It delivers one authenticated `LogTransaction` at a time, with
both additions and retractions (including noHistory log information). It never
substitutes current-index facts for the log. No next event is delivered until
the previous event is acknowledged; caller-retained events are caller memory.

```rust,no_run
use atomic_core::{ChangeConsumer, ChangeConsumerConfig, postgres_config_from_env};
use std::time::Duration;
# fn main() -> Result<(), Box<dyn std::error::Error>> {
let mut changes = ChangeConsumer::connect_configured(
    postgres_config_from_env()?, "orders", "invoice-export",
    ChangeConsumerConfig::default(),
)?;
while let Some(event) = changes.next(Duration::from_secs(30))? {
    // Apply an idempotent external effect using lineage/generation/t/hash
    // as its identity. Do that work before acknowledging this event.
    println!("transaction {}: {} datoms", event.transaction.t, event.transaction.data.len());
    changes.acknowledge(&event.checkpoint)?;
}
# Ok(()) }
```

A new name begins at genesis in the current generation; an existing name resumes
after its last acknowledged transaction. Checkpoints store lineage, generation,
last T, commit hash and CAS revision, and are reauthenticated on reopening.
Only the consumer's actually delivered, contiguous token is acknowledged.
Concurrent instances under the same login/name may perform duplicate work; a
stale acknowledgment returns `consumer/checkpoint-conflict`, never overwrites
newer progress. Reopen before continuing. A process crash before acknowledgment
replays the event. An ambiguous checkpoint write requires reopening to resolve
persisted progress. There is no exactly-once external-side-effect guarantee.

The event byte limit is operational admission, not a restriction on legal
transactions. Payload length is checked before transfer; encoded payload and
decoded datom bytes are accounted, not allocator RSS. One oversized transaction
returns `consumer/event-too-large` without advancement. Reopen with sufficient
capacity. Traversal work is one authenticated transaction, not all backlog.
Cancellation/timeout bound idle waits; already-issued SQL is subject to the
configured PostgreSQL I/O policy. No background event-prefetch thread exists.

Checkpoints are not retention pins or part of backups' canonical database data.
Each read takes only a transaction-scoped generation pin. Excision changes the
generation: existing consumers and old checkpoints fail explicitly with
`consumer/generation-changed`, even when old bytes remain retained. Choose a new
consumer name deliberately to process the current retained generation; old
events already copied by an application are not erased. Missing retained history
and lineage replacement also fail explicitly, not by silently skipping ahead.

The current schema baseline includes application checkpoint state. Initialize a
fresh PostgreSQL catalog with `atomic migrate`; historical catalog schema upgrades
are unsupported. Runtime provisioning gives peer/writer roles SELECT/INSERT/UPDATE
on the checkpoint table, not canonical write powers.
Row-level security restricts checkpoint rows to `current_user`; a name is shared
only within one SQL login and logical database. Existing PostgreSQL schema/role
scoping still governs database access; consumer names are not a tenant ACL.
The catalog owner is administrative and can inspect/repair checkpoint rows.

## Focused acceptance

With an authorized disposable PostgreSQL fixture configured, run
`cargo test --test change_consumer -- --nocapture --test-threads=1`.
The suite covers restart/replay/CAS, malformed and oversized checkpoints/events,
listener disconnect, generation change, restricted-role isolation, and a separate
supported writer process waking an independent peer and an already-waiting
consumer. The process witness prints reader idle SQL and commit-to-observation
times. These are bounded debug-build samples, not a throughput or scaling claim;
the writer process's lease-renewal SQL is separate from reader-process totals.
