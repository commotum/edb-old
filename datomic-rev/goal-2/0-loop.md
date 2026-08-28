# Goal 2 Working Loop

1. Read `0-plan.md` and sync it with the actual repository, worktree, artifacts,
   retained evidence, running processes, and completed results. Do not assume a
   prior status claim is still true.
2. Select the first unfinished stage and identify the smallest meaningful
   outcome that directly advances its completion signal.
3. Act using current evidence and best judgment. Prefer deterministic recovery,
   exact source ownership, and bytecode- or behavior-constrained decisions over
   speculative cleanup or broad rewrites.
4. Verify important outcomes in proportion to their risk. Keep candidate and
   licensed-oracle classpaths separate; test failure and cleanup paths; use
   disposable PostgreSQL and bounded processes for service work.
5. Fold material findings, changed assumptions, decisions, evidence paths and
   hashes, and honest stage status back into `0-plan.md`. Revise stage strategy
   when facts require it without weakening the original objective.
6. Continue through the stages in order unless evidence establishes a real
   dependency change. Do not advance merely because a partial demo works.
7. At the end of any incomplete session, leave a concise continuation note in
   `0-plan.md` stating the current stage, what was proven, what remains, and the
   next direct action.

## Operating principles

- Preserve the recovered Peer as the reference boundary and keep `goal-1`
  untouched unless the user explicitly changes scope.
- Recover the complete Transactor artifact before making subsystem-completeness
  claims; do not equate namespace-name overlap with implementation identity.
- Keep PostgreSQL primary and alternative storage backends deferred until the
  plan reaches them.
- Treat third-party services and libraries as dependencies, not Datomic source
  recovery targets; recover Datomic's semantics and glue around them.
- Prefer evidence that makes false success difficult: exhaustive manifests,
  exact identities and bases, isolated runtimes, bounded waits, strict cleanup,
  and post-failure state checks.
- Preserve unrelated worktree changes and avoid destructive actions.
- Keep records concise enough to support action. Match completion language to
  observed results and report blockers or uncertainty plainly.
