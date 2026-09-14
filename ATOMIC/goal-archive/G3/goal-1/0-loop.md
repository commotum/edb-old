# Goal 1 Execution Loop

1. Read this goal's `0-plan.md` and the current `goal-0/0-plan.md`. Reconcile
   the baseline with actual code and results; archived evidence is context.
2. Select the first unfinished stage and take the action most directly
   advancing its outcome. Begin with the actual build failure, not another
   broad review. Use relevant docs and recovered source when semantics matter.
3. Implement a coherent repair and run checks suited to that boundary. Preserve
   existing behavior, migrations, and unrelated edits. Establish the public
   application workflow as soon as its prerequisites work.
4. Prove PostgreSQL tests execute on disposable fixtures. Distinguish failures,
   missing prerequisites, and tests that did not run. Repair baseline defects;
   retain concrete later-stage failures and their parent ownership without
   weakening assertions or concealing limitations.
5. Record only material findings, decisions, and stage status in `0-plan.md`.
   Continue through this goal's stages, revising tactics from current evidence.
6. When the exit condition is met, update Goal 0's Stage 1 and remaining context,
   then return to `goal-0/0-loop.md` to scaffold/resume Goal 2 and continue toward
   the full objective. Do not create sub-goals beneath Goal 1.
7. At an unavoidable session boundary, leave both plans with the active stage,
   last verified result, concrete remaining issue, and next useful action.
   Report a blocker plainly if progress requires unavailable information or
   authority; otherwise continue within the authorized work.
