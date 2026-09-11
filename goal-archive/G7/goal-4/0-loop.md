# Goal 4 — Execution loop

1. Reconcile this plan with Goal 0, current code and verified tests.
2. Select the first unfinished internal stage; implement the next useful slice
   with native Rust and the shared opaque-store boundary. Create no child goals.
3. Exercise actual PostgreSQL/service/application paths and permanent regressions.
   Measure complete costs and inspect architectural ownership, not just answers.
4. Preserve exact receipt-first retries, reader/build protection and current-version
   semantics; port useful tests and delete displaced code as responsibility moves.
5. Fold material decisions/results/status into both plans; continue until this
   child's full completion signal holds, then return to Goal 0 Stage 5.
6. At session boundaries leave one concise next-action note. Report gaps plainly;
   no compatibility project, silent feature reduction or parent-completion claim.
