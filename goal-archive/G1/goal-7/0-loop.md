# Goal 7 Working Loop

1. Reconcile `goal-7/0-plan.md`, Goal 0, the repository, and verified Goal 1–6
   contracts. Start at the first Goal 7 stage without real evidence.
2. Before fixing a boundary, trace the relevant transaction/HA/error docs and
   `1.0.7705` transactor, peer `ConnectionState`, bounded queues, pending
   promises, report queue, reconnection, cluster coordination, and anomaly
   paths. Preserve intent and useful structure; do not clone JVM messaging.
3. Implement the smallest working Rust/PostgreSQL increment. Keep semantic
   assessment in the kernel, durable publication in `PostgresStore`, and add no
   generic storage/coordinator or unnecessary network/service framework.
4. Verify the risky claim at its actual boundary: real PostgreSQL for leases
   and fences, independent connections/processes for leadership, bounded-load
   tests for queues, and restart/fault injection for unknown outcomes.
5. Record material semantics, source mappings, deviations, measured limits,
   and truthful evidence directly in the plan. Mark only behavior observed by
   tests complete.
6. Continue through all stages. A lease table without publication fencing, a
   worker without bounded failure behavior, or a happy-path failover demo is
   not completion.
7. At the exit audit, verify one stale-epoch publication attempt is rejected,
   idempotency resolves ambiguous retry exactly once, pending requests settle,
   reports and reads behave across failover, and the full prior suite passes.
8. When complete, update Goal 0 Stage 7 and return immediately to its parent
   loop. If genuinely blocked, record the blocker and next unblocking action in
   both plans; otherwise keep shipping behavior instead of ceremony.
