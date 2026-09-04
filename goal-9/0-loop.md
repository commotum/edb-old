# Goal 9 Corrective Parent Loop

1. Read `goal-9/0-plan.md`, `goal-9/EVIDENCE_LEDGER.md`, and the reopened
   `goal-0/0-plan.md`; reconcile them with the working tree and actual test
   execution.
2. Select the first unfinished Goal 9 stage. Keep exactly one corrective child
   active. Stage 1 maps to `goal-10/`, Stage 2 to `goal-11/`, and so on through
   Stage 11 at `goal-20/`.
3. If that child folder is absent, use `$scaffold-goal` to create exactly its
   `0-plan.md`, `0-loop.md`, and `0-prompt.md`. If it exists, preserve and resume
   it. Creating a scaffold is never stage completion.
4. Before a material design change, trace the ledger entry back to both the
   documentation and relevant `1.0.7705` namespace/type/control flow. Resolve
   disagreement in favor of `datomic_pro_docs`; label Rust/PostgreSQL safety
   decisions as native rather than pretending they are recovered requirements.
5. Make the shortest coherent implementation change that advances the child.
   Retain working behavior and tests unless the source-backed contract requires
   changing them. Do not add compatibility or abstraction outside Goal 0.
6. Verify the risky boundary with a regression fixture plus the appropriate
   unit/property/differential, PostgreSQL, concurrency, restart, fault, or
   measured scale evidence. A PostgreSQL test that returns because configuration
   is absent is not PostgreSQL evidence.
7. Record only material decisions, evidence, deviations, and truthful status in
   the child plan. When its full exit condition is met, update this plan and the
   owning reopened Goal 0 stages, then immediately start the next child.
8. After all children appear complete, run the integrated Goal 20 gate and
   audit the original Goal 0 objective rather than child labels. Reopen the
   owning child for any gap.
9. Stop only for a concrete blocker requiring user authority or an external
   state that cannot be safely provisioned. Otherwise leave any unavoidable
   session boundary with the active child, last verified behavior, and next
   state-changing action explicit.

Prioritize executable corrections over more planning. Do not re-litigate
findings marked refuted in the ledger without new evidence.
