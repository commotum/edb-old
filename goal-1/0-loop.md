# Goal 1 Working Loop

1. Read `0-plan.md` and sync every stage status with the actual repository, build artifacts, validation results, and recorded evidence. Do not rely on a prior completion claim without checking its evidence.
2. Select the first stage whose completion signal has not been satisfied.
3. Choose and perform the smallest coherent body of work that directly advances that stage, using current evidence and best judgment.
4. Validate important outcomes in proportion to risk. Prefer clean, repeatable checks against the source-built artifact and make original-artifact oracle use explicit.
5. Record material findings, commands or entry points, hashes, seeds, failures, decisions, and changed assumptions concisely. Fold them into `0-plan.md` and update stage status only when observed results justify it.
6. Continue through the ordered stages. Do not begin speculative warning cleanup or readability work before the preceding verification stages meet their completion signals.
7. For warning repairs, require bytecode evidence that uniquely establishes the change. Never guess or suppress an unresolved warning; document it and continue with other evidence-backed work.
8. Before ending a session, leave a brief continuation note in `0-plan.md` identifying the current stage, verified state, unresolved issue, and most direct next action.
9. Declare the goal complete only after all five completion signals have been rechecked against the current repository state. Report blockers, uncertainty, and remaining limitations plainly.

## Working Principles

- Advance observable project capability and evidence, not activity counts.
- Keep source-only provenance, behavioral fidelity, reproducibility, and safe test isolation intact.
- Let results revise tactics while preserving the objective and stage order.
- Prefer durable automation and concise evidence over one-off successful commands.
- Distinguish verified equivalence, expected differences, and unknown behavior.
