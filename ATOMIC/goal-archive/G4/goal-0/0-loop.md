# Goal 0 Execution Loop

1. Read this plan and reconcile current stage/status with code and actual
   results. Preserve completed repairs; archived G1/G2/G3 are evidence only.
2. Select the first unfinished stage and matching goal-N (Stages1–7 → Goals1–7).
   Reconcile/resume an existing child; use $scaffold-goal for a missing child's
   0-plan.md, 0-loop.md and 0-prompt.md. Keep one child active and no nested goals.
3. Execute the smallest coherent delivery in that child using local Datomic
   docs, relevant recovered source and best judgment. Extend the common real
   application and existing test/measurement support as work progresses.
4. Verify changed boundaries with focused semantics, actual PostgreSQL and
   appropriate security/failure/cost checks. Record seed/replay/reduced failures
   as applicable. Reuse prior evidence; broaden for actual risk, not green totals.
5. Fold material decisions, results, limits and status into child and parent
   plans. Preserve all assigned capabilities and invariants; ask before a
   material scope change. Keep records concise and avoid repeated roadmap copies.
6. On child completion, return to Step1 and execute the next unfinished child.
   Scaffolding or one child's completion is not the finish line. Final gaps
   reopen their owner; close Goal0 only at integrated product acceptance.
7. At an unavoidable session boundary record active child, last verified result,
   blocker/uncertainty and next concrete action. Do not pause solely because a
   child finished while safe in-scope work remains.
