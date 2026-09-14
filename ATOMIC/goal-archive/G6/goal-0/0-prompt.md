# Goal 0 continuation prompt

```text
Complete Atomic's required native capability stages using
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.
The dated audit is goal-0/1-audit.md; preserve its evidence and completed Goal 1 EDN.

Implement equivalents in spirit: use Datomic Pro docs for user-visible semantics
and 1.0.7705 for architectural evidence, but choose idiomatic Rust and PostgreSQL,
not literal Clojure/JVM internals. The user's fresh-database policy in the parent
plan supersedes all older compatibility requirements: database schema/formats may
change without migrating old development databases. Do not spend further work on
old-binary/legacy-format upgrade matrices or mixed-version rolling upgrades.
Preserve immutable values, identity/schema, current-version durable correctness,
prior semantic repairs and receipt-first exact retries. Use fresh current-version
fixtures and verify restart/crash recovery, same-version failover and backup/restore.
Reject unsupported formats clearly; never silently reinterpret or automatically
reset them. Keep existing compatibility code unless a current change needs to
simplify it. Keep the end user and usable public/application workflows central.
Apply the parent's proportionate-implementation policy: focused tests, reuse
unaffected evidence, representative integration and measurements only where useful.
No duplicate acceptance matrices, invented hard resource guarantees, literal
Clojure component parity or completion ceremonies. Unapproved optional features
are deferred, not blockers; required capabilities and data safety remain intact.

Sync actual code/tests, select the first unfinished stage and matching Goal 2–10,
reconcile its existing scaffold (use scaffold-goal if missing), and execute through
its completion signal. Keep one child active, no recursive hierarchy. Fold results
into child/parent, return to Goal 0 and continue. Reopen owning children for
integration gaps. Required capabilities, optional decisions, invariants and
acceptance are in the plan; do not silently add services or remove core gaps.
Use permanent regressions, actual PostgreSQL/application checks and measured
complete-path costs. Leave concise continuation notes and report uncertainty
plainly. Finish only at integrated acceptance, not at a scaffold or one child.
```
