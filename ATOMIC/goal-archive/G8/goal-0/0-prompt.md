```text
Complete Atomic's source-grounded Rust/PostgreSQL cutover using
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.

Use datomic_pro_docs as semantic authority and 1.0.7705 as architectural evidence.
Preserve strong existing implementations and current user capabilities; correct
demonstrated behavioral gaps, architectural drift and unnecessary complexity.
Do not equate library presence or a missing Rust type with a missing feature.

Work component by component: read source and callers, annotate the WHY, trace
relevant doc passages, compare Rust mechanisms, then retain/move/adapt/replace
with evidence and verify the integrated result. Unrelated source study is not
a blanket implementation gate. Full source accounting, major WHY explanations
and important paragraph-level traces remain required final deliverables.

Organize shared mechanisms, peers and transactors around real responsibilities,
with modules and justified crate boundaries; organize user docs like the Pro
chapters. Full first-release cutover: no old-format compatibility, converters,
aliases or permanent dual engine. Remove superseded code/tests/docs while
retaining useful independent regressions. No JVM parity or other-store project.
Do not erase reference bodies/provenance or reset unrelated databases.

Sync actual state, execute the first unfinished stage and continue through all
stages. Use focused checks, real PostgreSQL/application workflows, architectural
inspection and measured complete-path costs. Skipped checks and prior reports
are not fresh verification. Keep one active stage/component, update the plan and
traces, and leave concise continuation notes. No recursive/corrective goals.
Finish only when the learning deliverables, organized product, cleanup and full
integrated acceptance hold; report blockers and uncertainty plainly.
```
