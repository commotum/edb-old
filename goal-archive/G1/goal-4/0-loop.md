# Goal 4 Working Loop

1. Read `goal-4/0-plan.md`, the completed Goal 2/3 status, and the current code/tests; reconcile stage status with actual index, migration, peer, and PostgreSQL behavior rather than assuming the scaffold is current.
2. Select the first unfinished stage and identify the smallest working behavior that materially advances its outcome and completion signal.
3. Read the relevant `datomic_pro_docs` contract, then trace the narrow corresponding `1.0.7705` database, index, btset, db-io, valcache, log, or peer-connection path before fixing a representation, cache, or synchronization rule.
4. Implement the closest clear Rust/PostgreSQL equivalent. Keep Goal 3's log/head authoritative, make index state explicitly derived and rebuildable, preserve immutable snapshots, and keep query/pull, generic storage, HA service, and lifecycle machinery out.
5. Differentially verify logical index results against the Goal 2 kernel. Use real PostgreSQL whenever work crosses artifact publication, restart, peer synchronization, consolidation, corruption, or process-failure boundaries; measure the performance assumption being encoded when physical layout is involved.
6. Record only material format decisions, recovered mappings, justified deviations, observed performance facts, and changed stage status in `goal-4/0-plan.md` or the smallest necessary contract artifact.
7. Continue through multi-peer and fault verification until the index/peer exit condition is demonstrated; do not equate types, migrations, repository methods, or a single happy-path peer with completion.
8. Before ending mid-goal, leave a concise continuation note naming the first missing acceptance behavior, the relevant basis/root/peer state, and the next state-changing implementation or test.

Prefer observable peer behavior and identical index results over storage frameworks. Treat a gap, snapshot mutation, corrupt reachable index artifact, consolidation result change, or unavailable real-PostgreSQL evidence as an explicit failure or blocker, never implicit success.
