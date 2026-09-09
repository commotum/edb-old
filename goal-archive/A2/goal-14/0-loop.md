# Goal 14 execution loop

1. Reconcile this plan with the current code, Goal 13's native snapshot API,
   `goal-9/EVIDENCE_LEDGER.md`, and the actual red/green test evidence.
2. Select the first unfinished stage and trace its observable rules in
   `datomic_pro_docs` plus the relevant 1.0.7705 `db`, `query`, `datalog`,
   `extensions`, `aggregation`, or `pull` path before choosing representation.
3. Add the smallest independent witness that exposes the semantic or scaling
   defect, then implement the closest idiomatic Rust version of the recovered
   design. Keep one exact database value threaded through the complete path.
4. Verify semantic changes against the eager oracle and performance changes
   against the native peer using measured reads/materializations; use real
   PostgreSQL when native snapshots, restart, or concurrency are involved.
5. Run focused gates first, then formatting, warning-denying Clippy, pure tests,
   and the relevant non-skipping integration tests. Do not count absent
   PostgreSQL as a pass.
6. Fold material evidence, decisions, deviations, and truthful stage status
   back into `0-plan.md`, Goal 9, and Goal 0. Preserve known Goal 15/16 failures
   for their owners rather than restoring retired flat/eager shortcuts.
7. Continue until the exit condition is established. If interrupted, leave the
   first failing witness and exact next action in the plan.

Prefer working behavior and executable evidence over extra process artifacts.
Treat a scaffold, type declaration, or compatibility materialization as no
substitute for the stage outcome.
