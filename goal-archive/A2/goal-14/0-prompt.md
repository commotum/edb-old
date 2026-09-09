```text
Continue Goal 14, exact database-value query, pull, and entity conformance for
the native Rust Datomic-inspired database, using
/home/jake/Developer/atomic/goal-14/0-plan.md and
/home/jake/Developer/atomic/goal-14/0-loop.md.

Treat datomic_pro_docs as semantic authority, Goal 13's lazy immutable
PeerSnapshot as the production read boundary, the eager Database as the
independent oracle, and 1.0.7705 Db/windowed/query/datalog/extensions/pull code
as the default blueprint. Rust throughout and PostgreSQL only. Make every
operation consume one exact immutable database value; correct source
propagation, pull/entity selectors, claimed query primitives and aggregates,
and invocation-scoped rules without generic storage abstraction, eager native
materialization, or fixed semantic iteration limits.

Sync the plan to actual artifacts and evidence, advance the first unfinished
stage with executable source-witness fixtures and working behavior, verify
eager/native equivalence and lazy bounded access with real PostgreSQL where
needed, and fold decisions and truthful status back into the plan. Continue
through the full exit condition; report genuine blockers or uncertainty
plainly and leave Goal 15/16 defects to their owning children.
```
