# Goal 1 Working Loop

1. Read `/home/jake/Developer/atomic/goal-0/0-plan.md` and
   `/home/jake/Developer/atomic/goal-0/0-loop.md`, then this child's `0-plan.md`.
   Reconcile their status with actual source, executables, tests and observed
   results. Preserve completed work; archived G1/G2/G3 goals are evidence only.
2. Select the first unfinished Goal 1 stage. Inspect the relevant existing APIs
   and semantic evidence, then take the actions that most directly make the
   ordinary local deployment usable. Use best judgment; no baseline rewrite,
   nested goal hierarchy or corrective parent.
3. Exercise the real executable and application workflow as implementation
   advances. Verify risky behavior on an isolated actual PostgreSQL fixture with
   appropriate roles and configured transport. Unconfigured tests that return
   early are not PostgreSQL evidence. Preserve exact outcomes, old values and
   bounded ordinary reads; do not mutate shared acceptance data casually.
   Document actual query/storage dependence, not just local `db()` capture or
   cursor node counts. Preserve live report-queue semantics and distinguish them
   from the later checkpointed durable consumer.
4. Fold material decisions, verified commands/results, failures and stage status
   into `0-plan.md`, and update Goal 0 when a finding changes the parent view.
   Keep records concise. Separate existing evidence from new observations,
   pending maintenance from corruption, and measured limits from assumptions.
   Hand off wakeup/replay and snapshot-reference work to Goal 2, cache/I/O work
   to Goal 3, query-source work to Goal 4 and scaling acceptance to Goal 7;
   do not pull their implementations into this child's completion gate.
5. Continue through this child's stages. A new executable, scaffold or passing
   unit test alone is not completion. Do not narrow the objective to hide an
   unresolved deployment/API gap; report blockers or required authority plainly.
6. Once Goal 1's objective and completion signals are established, return to
   Goal 0, reconcile remaining stages, then scaffold or resume Goal 2 under the
   parent loop. Keep one child active; do not execute unrelated later-stage work
   concurrently or treat this child's completion as the original finish line.
7. At a session boundary, leave a short continuation note in `0-plan.md`:
   current stage, last verified result, exact next action, live command handles
   if any, and any blocker or uncertainty. Resume rather than restart.
