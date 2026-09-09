# Goal 1 Working Loop

1. Read `goal-1/0-plan.md` and reconcile it with the actual semantic documents, `1.0.7705` evidence, existing decision records, model code, and test results.
2. Select the first unfinished stage or the earliest unsupported rule within it.
3. Read the relevant `datomic_pro_docs` files first, then trace the corresponding path through `1.0.7705`, including names, type boundaries, representations, algorithms, and performance-oriented structure.
4. State the rule in implementation-independent language, classify its authority, identify observable consequences, and map the recovered implementation to its closest idiomatic Rust form. Make adaptations and unavoidable project decisions explicit.
5. Encode the rule in the simplest useful artifact: a normative example, decision record, machine-readable fixture, property, or small reference-model behavior. Avoid production optimization and PostgreSQL layout work in this goal.
6. Run the relevant checks, including permutations where transaction order-independence matters and boundary/type combinations where equality or ordering matters.
7. Fold material findings, changed assumptions, resolved ambiguities, and truthful stage status back into `goal-1/0-plan.md`; keep detailed evidence in the supporting artifacts rather than bloating the plan.
8. Continue until the foundation exit condition is met. Before ending mid-goal, record a concise continuation note naming the next unsupported rule or failing fixture.

Prefer semantic closure and executable evidence over broad commentary. Neither discard recovered implementation details as merely incidental nor copy JVM-specific machinery mechanically: understand the role, preserve it by default, and document why a Rust adaptation is better. Never claim a rule is settled when the sources or tests still disagree.
