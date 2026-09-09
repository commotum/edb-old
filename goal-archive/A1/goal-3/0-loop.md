# Goal 3 Working Loop

1. Read `goal-3/0-plan.md`, the completed Goal 2 status, and the current code/tests; reconcile stage status with observed files and real PostgreSQL behavior rather than assuming the scaffold is current.
2. Select the first unfinished stage and identify the smallest durable, testable increment that advances its outcome and completion signal.
3. Read the relevant `datomic_pro_docs` contract, then trace the narrow corresponding `1.0.7705` `log`, `kv-store`, `kv-sql`, SQL, catalog, or recovery path before fixing a representation or commit rule.
4. Implement the closest clear Rust/PostgreSQL equivalent. Keep the kernel transition pure, use one concrete PostgreSQL design, and keep peer, HA service, query/pull, background index consolidation, and generic storage machinery out.
5. Verify encoding locally and persistence semantics against real PostgreSQL. Add fault or concurrency coverage whenever an increment crosses publication, acknowledgment, retry, restart, or corruption boundaries.
6. Record only material durable-format decisions, recovered mappings, justified deviations, and changed stage status in `goal-3/0-plan.md` or the smallest necessary durable contract artifact.
7. Continue through recovery and the fault matrix until the durability exit condition is actually demonstrated; do not equate migrations, repository methods, mocks, or happy-path restarts with completion.
8. Before ending mid-goal, leave a concise continuation note naming the first missing acceptance behavior, the PostgreSQL state involved, and the next state-changing test or implementation action.

Prefer verified commit/recovery behavior over storage frameworks. Treat unknown commit outcome, corrupt reachable state, or unavailable real-PostgreSQL testing as explicit conditions, never as implicit success.

