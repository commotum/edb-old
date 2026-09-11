# Goal 2 — Execution loop

1. Reconcile this plan and Goal 0 with actual code/tests; select the first
   unfinished internal stage. Keep this one child active; no grandchildren.
2. Implement selective block-backed structures and reuse existing traversal,
   recent-tier and value semantics. Consult local Datomic docs/source as needed.
3. Check both behavior and ownership: no old SQL catalog reads, whole-database
   recovery fallback or duplicate query engine hidden in the new path.
4. Run permanent focused tests and actual fresh-schema PostgreSQL/library
   workflows. Measure opening plus query/branch work, not only cached inner loops.
5. Record material decisions, actual results and remaining gaps here and in Goal 0.
   Continue until this child's completion signal holds, then execute Goal 0 Stage 3.
6. At a session boundary leave a concise next action; do not call fixture success
   or this child's completion the complete-product finish line.
