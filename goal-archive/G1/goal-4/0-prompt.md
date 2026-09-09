# Goal 4 Continuation Prompt

```text
Continue Goal 4, persistent indexes and peer read architecture for the native Rust database, using /home/jake/Developer/atomic/goal-4/0-plan.md and /home/jake/Developer/atomic/goal-4/0-loop.md.

Treat datomic_pro_docs as semantic authority, the completed Goal 1–3 contracts and tests as executable boundaries, and 1.0.7705 as the default blueprint for immutable database values, EAVT/AEVT/AVET/VAET and history indexes, base-plus-recent layering, log synchronization, peer state, caches, and consolidation. Use Rust throughout and PostgreSQL only. Keep Goal 3's transaction log and head authoritative; all persistent indexes and roots must be versioned, content-validated, derived, rebuildable, and unable to expose unpublished data. Preserve immutable old snapshots while independent peers advance monotonically and answer raw index and temporal reads locally.

Sync the plan with current artifacts and real PostgreSQL evidence, advance the first unfinished stage with working behavior, differentially verify index results against the Goal 2 kernel, exercise multi-peer, restart, consolidation, cache, corruption, and process-failure boundaries, and fold material decisions and truthful status back into the plan. Do not introduce generic storage abstraction, Datalog/query/pull, full-text, HA leadership, production transaction-service ceremony, or lifecycle work. Continue until multiple peers can lag and catch up without gaps, old snapshots remain exact, consolidation cannot change results, valid persistent roots bound restart work, and all index/view behavior matches the kernel oracle; report genuine blockers or uncertainty plainly.
```
