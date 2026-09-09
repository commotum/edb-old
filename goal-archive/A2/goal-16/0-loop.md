# Goal 16 Execution Loop

1. Sync `0-plan.md` with the repository, Goal 9's evidence ledger, completed
   Goals 10–15, and actual PostgreSQL measurements. Locate every production
   path that owns or constructs an eager `Database` before changing it.
2. Select the first unfinished stage. Trace the relevant documented behavior
   and recovered 1.0.7705 `Db`, transaction, indexer, log, and update control
   flow; record only consequential evidence or deviations.
3. Start with a decisive failing semantic or residency witness. Implement the
   smallest coherent Rust/PostgreSQL slice that removes a real eager dependency
   while preserving one fenced authoritative publication path.
4. Differentially compare the slice with the eager oracle, including errors
   and persisted functions/predicates. Measure decoded ranges, retained recent
   bytes, cache bytes, commitment work, and restart base/tail work; never infer
   boundedness from type names.
5. Exercise risky boundaries on real PostgreSQL: same-database contention,
   head changes, unknown retry, root repair, consolidation handoff, corrupt
   nodes, restart, generation change, process death, and standby takeover. A
   skipped integration binary is not evidence.
6. Fold verified behavior, measurements, source rationale, and remaining red
   witness into the plan. Continue to the next stage only when the current
   completion signal is observed.
7. Before closure, search production service/store/report/recovery paths for
   eager `Database`, `recover_to`, whole-index `datoms`, compatibility
   materialization, or unbounded retained reports. Run format, warning-denying
   Clippy, pure differential/property tests, and the focused live matrix.
8. When the exit condition is established, update Goal 9 and Goal 0 and return
   immediately to the parent loop for Goal 17. If blocked, record the concrete
   red witness and required unblocking action without narrowing the objective.

Prefer direct working slices over framework layers. Reuse the concrete native
tree, recent tier, metadata, program cache, fencing, and PostgreSQL contracts;
do not manufacture backend abstraction or ceremonial intermediate systems.
