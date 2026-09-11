```text
Rebuild Atomic as a source-grounded, idiomatic Rust realization of Datomic's
behavior, architecture and principles using
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.

First explain the recovered 1.0.7705 implementation with inline WHY commentary,
then trace important datomic_pro_docs passages to implementing source, then
fully cut over Atomic into coherent peer, transactor and shared components with
user docs organized like the Pro docs. Distinguish observed mechanisms,
documented rationale, inference and unknowns. Account for every source file
without repeating identical artifacts or porting compiler/dependency boilerplate.
Learn from and preserve the source mechanisms, not just similar test outputs.

Native Rust/PostgreSQL only. Full first-release cutover: no old formats,
compatibility shims, migrations, rollback converters or permanent dual engine.
Preserve user capabilities and current-version correctness, not existing code
structure. Do not erase reference bodies/provenance or reset unrelated databases.

Sync actual state, execute the first unfinished stage, and continue through all
stages using best judgment. Keep the plan and source/doc/Rust traces current;
remove superseded code, tests and docs as replacements take over. Use focused
regressions, real PostgreSQL/application checks and measured complete-path costs.
Avoid recursive scaffolds and duplicate process artifacts. Leave a concise
continuation note when needed. Finish only when the learning artifacts and the
full organized product, cleanup and integrated acceptance hold; report blockers
and uncertainty plainly. A scaffold or completed stage is not the finish line.
```
