# Goal 0 — Parent execution loop

1. Read `0-plan.md` and sync its statuses, decisions and continuation note with
   actual code and tests. Archived goals and historical measurements are evidence,
   not requirements. Preserve the architectural boundary and first-release policy.
2. Select the first unfinished stage. Stages 1–7 map to root-level `goal-1` through
   `goal-7`. Reconcile and resume its existing scaffold; if absent, use
   `$scaffold-goal` to create its three files. Keep one child active and no recursive
   goal hierarchy. Creating a scaffold is not executing the stage.
3. Execute that child through its observable completion signal using current
   evidence and best judgment. Consult relevant Datomic docs/source at the point of
   design, reuse working Rust components and take the next implementation step.
   Do not spend the stage producing exhaustive inventories or another parent plan.
4. Check ownership as well as behavior: does Rust own the database structure and
   policy, with PostgreSQL supplying opaque storage and generic atomic primitives?
   Reject a facade over the old tables, relocated SQL state machines or a hidden
   fallback as substitutes for the cutover. Keep protection/fencing coherent across
   writers, readers, background jobs and reclamation.
5. Exercise relevant permanent regressions and the real application/PostgreSQL path.
   Record which checks actually ran, failures and representative complete-path costs.
   Use fresh isolated targets; no destructive resets of unrelated databases.
6. Remove superseded code, SQL, tests and fixtures as their responsibilities move.
   Preserve/port useful behavior tests, not historical layouts or upgrade support.
   Temporary development overlap must have an owning stage and disappear by final
   acceptance; never introduce a compatibility product or require reversible cutover.
7. Fold material decisions/results and truthful status into the child and this
   parent plan. Return to Goal 0 and execute the next unfinished child. Reopen an
   owning stage when integrated checks reveal a gap rather than masking it with an
   exclusion or creating a corrective parent.
8. Finish only when the entire product and the parent's architectural, cleanup and
   integrated acceptance hold. At a session boundary, leave a concise note naming
   the active stage, completed changes/checks, next action and any concrete blocker.
   Report uncertainty plainly; a child or documentation update is not the finish line.
