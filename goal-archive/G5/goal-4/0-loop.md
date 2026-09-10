# Goal 4 execution loop

1. Reconcile `0-plan.md`, Goal0 Stage4 and current code/tests. Preserve completed
   work and keep Goal4 as the only active child; no grandchildren.
2. Select the first unfinished work item and directly reproduce, repair or verify
   it. Use docs/source for semantics; parallel disjoint R7/R8 work is permitted.
3. Measure complete bookkeeping/validation paths, including maintained statistics,
   allocation/cleanup and relevant locking; independently check totals and results.
4. Run permanent and affected regressions plus real PostgreSQL/application checks.
   Distinguish a skipped fixture or permission failure from executed product proof.
5. Fold material decisions/results/status into this plan and Goal0. Reopen owning
   earlier children for integrated defects without silently reducing requirements.
6. Continue until the completion signal is observed, then return to Goal0 and
   execute Stage5. This child's completion does not finish the parent objective.
7. At a session boundary leave a concise next-action note and state blockers plainly.
