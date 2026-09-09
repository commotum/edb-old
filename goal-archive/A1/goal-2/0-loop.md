# Goal 2 Working Loop

1. Read `goal-2/0-plan.md`, `goal-1/SEMANTICS.md`, and the current code/tests; reconcile stage status with observed behavior rather than assuming the scaffold is current.
2. Select the first unfinished stage and identify the smallest working kernel increment that advances its outcome and completion signal.
3. Read the relevant `datomic_pro_docs` contract, then trace the narrow corresponding path and named structures in `1.0.7705` before designing the Rust implementation.
4. Implement the closest clear Rust equivalent in the production path. Keep `with` pure and unordered, preserve immutable inputs, and avoid PostgreSQL, distributed machinery, full query/pull work, or generic storage abstraction.
5. Verify the increment against the Goal 1 oracle and focused unit, permutation, property, index cross-check, or state-machine tests suited to the risk. A plan or type skeleton is not a completed increment.
6. Record only material semantic findings and significant deviations from `1.0.7705`, with rationale. Fold truthful stage status and changed facts back into `goal-2/0-plan.md` without using it as a work log.
7. Continue through the stages toward the kernel exit condition. Remove or consolidate the scan reference only after differential evidence makes it redundant.
8. Before ending mid-goal, leave a concise continuation note naming the first failing or missing acceptance behavior and the next state-changing action.

Prefer shipped, verified kernel behavior over framework-building. Match completion language to checks actually run, and report unresolved semantics or failures plainly.

