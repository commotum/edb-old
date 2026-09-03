# Goal 0 Working Loop

1. Read `goal-0/0-plan.md` and reconcile every status and assumption with the repository, child-goal plans, tests, recorded decisions, `datomic_pro_docs`, and relevant `1.0.7705` evidence.
2. Select the first parent stage whose completion signal is not actually established. Never skip an incomplete or blocked stage merely because a later scaffold exists.
3. Map that stage to `goal-N/`, where `N` is its stable parent-stage number.
   - If the folder is missing, use `$scaffold-goal` to create exactly `0-plan.md`, `0-loop.md`, and `0-prompt.md`. Derive the child objective, constraints, known context, broad stages, and exit condition from the full current parent plan and accumulated evidence.
   - If the folder exists, preserve it, reconcile its claims with reality, and resume its first unfinished child stage. Do not regenerate it or create `goal-(N+1)`.
4. Execute the child goal, following its plan and loop, until its full exit condition is demonstrated. Trace the relevant `1.0.7705` types, representations, control flow, caches, concurrency, and algorithms before fixing each important design; use `datomic_pro_docs` as semantic authority and the completed earlier goals as executable boundaries.
5. Confirm the child outcome with checks proportionate to its risks: semantic fixtures, unit/property/differential tests, real PostgreSQL integration, concurrency, restart, process failure, corruption injection, or measured performance evidence as applicable. A scaffold or plausible implementation is not completion.
6. Record material findings, deliberate decisions, PostgreSQL assumptions, limitations, and significant source deviations in the child plan. Mark the child complete only when its stated exit condition is met.
7. Return to `goal-0/0-plan.md` immediately after the child completes. Update the matching parent-stage status with concise evidence, then reread and revise all unfinished parent stages whose boundaries, dependencies, or completion signals should change in light of what was learned. Preserve the original objective and completed stage numbering.
8. Start the loop again at step 1 and create or resume the new first unfinished child goal. Do not stop merely because one child completed while another parent stage can safely proceed.
9. When all numbered stages appear complete, verify the Goal 0 success condition across the integrated system rather than trusting child statuses. Record and repair any gap through the owning child goal; finish only when the overall condition is observed.
10. If a genuine blocker prevents further safe work, record it and the next unblocking action in both the active child and parent plans. Otherwise, before any unavoidable session boundary, leave a concise continuation note identifying the active child, first unfinished behavior, current evidence, and next state-changing action.

Prioritize working, verified system increments over scaffold production, framework-building, or speculative abstraction. Keep one active child, match every completion claim to observed behavior, and report blockers or uncertainty plainly.
