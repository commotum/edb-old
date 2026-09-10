# Goal 0 execution loop

1. Read `0-plan.md`; reconcile stage status with actual code, tests and material
   decisions. Preserve completed repairs. Past review measurements are context,
   not current acceptance. Keep the twelve-finding coverage map accurate.
2. Select the first unfinished stage. StageN owns `../goal-N`. Reconcile an
   existing child's work; use `$scaffold-goal` to create a missing child's
   `0-plan.md`, `0-loop.md`, `0-prompt.md` from that stage. Keep one child active,
   with no grandchildren or corrective parent. Goal1 already exists.
3. Execute the child, not just its scaffold. Choose the next action that directly
   advances its outcome: normally a reproducible regression followed by the shared
   repair. Use docs/source evidence where semantics are unclear, and avoid unrelated
   cleanup. Preserve data, prior repairs and compatibility boundaries.
4. Verify its observable completion signal with checks proportional to risk,
   including actual PG/application paths and complete-path measurements where
   specified. A missing fixture, ignored test or truncated run is not a pass.
5. Fold material changes, decisions, commands/results and remaining uncertainty into
   the child plan and this parent plan. Mark the stage complete only on its signal.
   If a finding is disproved, retain evidence that explains the original observation.
6. Return here, reconcile dependencies, then scaffold/resume and execute the next
   unfinished child. Continue through integrated Stage7. Reopen the owning child
   for an integrated regression instead of creating another hierarchy or excluding it.
7. At a session boundary, leave a concise note in the active child and parent:
   last verified state, unresolved issue, next concrete action and required fixture
   or authority. Report genuine blockers plainly; do not claim completion or expand
   scope to work around an unavailable prerequisite.

Finish only when the original repaired-product objective and integrated acceptance
are established. Keep records useful and short; do not create process work in place
of implementation or turn optional observations into new mandatory features.
