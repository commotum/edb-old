# Goal 0 Working Loop

1. Read `goal-0/0-plan.md`, then reconcile its stage status and assumptions with the repository, test results, recorded decisions, `datomic_pro_docs`, and relevant `1.0.7705` evidence.
2. Select the first unfinished stage and identify the smallest work that materially advances its stated outcome and completion signal.
3. Trace the relevant path through `1.0.7705`, including its named types and boundaries, before implementing. Preserve its architecture, algorithms, invariants, and performance insights by default, translating them idiomatically into the Rust-only runtime and PostgreSQL-only storage design.
4. Confirm important outcomes with proportionate checks: semantic fixtures, unit and property tests, model comparison, SQL integration tests, crash/restart tests, concurrency tests, or failure injection as appropriate.
5. Record material findings, deliberate semantic decisions, PostgreSQL assumptions, observed limitations, and significant departures from `1.0.7705` with their rationale. Fold changed facts and stage status back into `goal-0/0-plan.md` without turning it into a work log.
6. Continue through the stages in dependency order while allowing evidence to refine tactics and stage boundaries, not the original objective.
7. Before ending a session, leave the tree in a truthful state and record a brief continuation note identifying the current evidence, unresolved issue, and next concrete action.

Prioritize working, verified system increments over framework-building or speculative abstraction. Match every completion claim to observed behavior, and report blockers or uncertainty plainly.
