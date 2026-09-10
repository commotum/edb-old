# Goal 0 continuation prompt

```text
Complete Atomic's required native capability stages using
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.
The dated audit is goal-0/1-audit.md; preserve its evidence and completed Goal 1 EDN.

Implement equivalents in spirit: use Datomic Pro docs for user-visible semantics
and 1.0.7705 for architectural evidence, but choose idiomatic Rust and PostgreSQL,
not literal Clojure/JVM internals. Preserve immutable values, identity/schema,
durable bytes, prior repairs and receipt-first exact retries. Keep the end user
and usable public/application workflows central.

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
