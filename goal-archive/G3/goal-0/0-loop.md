# Goal 0 Execution Loop

1. Read `0-plan.md` and reconcile the active stage with current code, actual
   test execution, and material results. Consult relevant archived evidence
   selectively; archived loops and completion labels are not active policy.
2. Select the first unfinished stage and its matching repository `goal-N/`
   child. If absent, use `$scaffold-goal` to create `0-plan.md`, `0-loop.md`,
   and `0-prompt.md` for that stage, inheriting the parent objective and
   constraints. If present, reconcile and resume it without overwriting work.
   Keep the current child active until its outcome is established; scaffolding
   alone is not completion or a reason to end parent execution.
3. Execute the child's loop using current evidence and best judgment. Use the
   local Datomic docs for semantics and relevant recovered 1.0.7705 paths for
   design. Exercise the ordinary application workflow throughout; do not defer
   integration until the last stage. Fix necessary prerequisites across stage
   boundaries while retaining one clear active focus.
4. Verify in proportion to risk: focused semantic/differential tests, real
   PostgreSQL, failure/restart checks, or measured workload costs. Confirm that
   integration tests actually execute. Rerun or broaden checks when a change,
   failure, or unresolved concern warrants it, not to accumulate passing totals.
5. Record material findings and evidence in the child plan. Once its completion
   signal is established, update the matching stage in this parent plan with
   the outcome, significant deviations, and validation limits. Reconcile the
   remaining stages against what was learned without weakening the objective.
6. Return to Step 1 and scaffold or resume the next unfinished child. Continue
   this cycle through all stages; do not stop after a child completes. Avoid
   recursive goal hierarchies or new corrective parents. Before final closure,
   compare the integrated system with the original objective and meaningful
   guarantees. Reopen the owning child for any unmet core outcome and repeat.
7. At an unavoidable session boundary, update both plans with the active child,
   last verified result, concrete remaining failure, and next useful action.
   Report a blocker plainly when further work requires unavailable information
   or authority; otherwise continue within the authorized objective.

Preserve working code and data. Prefer direct repairs and useful conformance
examples over exhaustive decompilation proofs, repeated audits, or status churn.
