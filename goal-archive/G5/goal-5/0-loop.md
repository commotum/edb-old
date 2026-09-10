# Goal 5 execution loop

1. Read `0-plan.md` and Goal0 Stage5; reconcile existing code and test evidence.
2. Select the first unfinished work item and directly reproduce/repair it using
   docs/source evidence where semantics are unclear. Preserve earlier repairs.
3. Verify results, view/budget safety and complete consumption costs, including
   actual PostgreSQL/application checks. Early returns and interrupted runs are
   not passes; required filtered/fallback scans must remain honestly accounted.
4. Fold material decisions, results and remaining gaps into this plan and Goal0.
5. Continue until R5/R9 and this child's completion signals are established, then
   return to Goal0 to execute Stage6. Keep one child active; no grandchildren.
6. At session boundaries leave the last verified state and next concrete action.

The parent remains unfinished until all repairs and integrated acceptance pass.
