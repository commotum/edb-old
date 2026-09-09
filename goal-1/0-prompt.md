# Goal 1 Continuation Prompt

```text
Make the existing native Rust/PostgreSQL database usable without example
harnesses: a configurable local transactor executable, safe administrative CLI,
reproducible separate-application deployment, and public db_stats, asynchronous
request_index and time-ordered UUID helpers. Preserve the working database and
its immutable values, serialized transactions, exact retry and peer-local reads.

Use /home/jake/Developer/atomic/goal-1/0-plan.md and
/home/jake/Developer/atomic/goal-1/0-loop.md under the parent
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.
Use /home/jake/Developer/atomic/datomic_pro_docs as semantic authority and
/home/jake/Developer/atomic/1.0.7705 as architectural evidence. Archived G1/G2/G3
goals are evidence, not active instructions.

Sync the plans with actual code and results, select the first unfinished child
stage, and execute through its observable completion signal using best judgment.
Build on existing service/operator APIs; do not rewrite the baseline or wrap an
acceptance example as the product. Use restricted roles, explicit PostgreSQL
TLS/I/O policy and safe local endpoint/administrative controls. Coordinate the
endpoint contract with Goal 2 without requiring its cross-host implementation.
Prove the real executable and public application workflow on actual PostgreSQL;
do not count self-skipped tests or ordinary eager recovery as acceptance.
Demonstrate one calculation accepting &DatabaseValue: capture a native value
once for query/entity navigation, reproduce its result after later commits, and
run the same function against a complete fabricated in-memory Database using
existing new/with/database_value APIs. This complements real deployment checks
and does not depend on raw-tuple query sources or a new store.
Document actual cached-query storage dependence and I/O-counter scope; local
db() capture is not proof of offline queries, and live lossless report queues
are not durable restartable consumers. Reuse existing configuration policy;
do not add flags for unimplemented caches or compression. Coordinate later
snapshot/replay and committed snapshot/view key comparison with Goal 2;
cache/batched index I/O/versioned compression and measured shared/indexed
speculation with Goal 3; and query sources/preparation/grouped joins with Goal 4.
These implementations are not required to close this local-deployment child.
Reuse pure-model comparisons and existing fault hooks; retain seed/replay traces
for generated checks used here. Add small clock/transport seams only where useful
in changed lifecycle code. Goal 7 owns generated operation/failure campaigns,
trace minimization, the complete branch/compare/choose/revalidate/commit example
(a small helper only if needed) and a profiling-based pipeline decision. Deeper
pipelining needs measured justification and preserves serialized commit authority.
Do not require a simulator, testing framework, alternate store or blanket trait refactor.
Pure with already exists; preview success is not a later commit guarantee.

Record material decisions, verified results and status in the child and parent
plans. Keep one child active and leave a concise continuation note at session
boundaries. Report blockers and uncertainty plainly; do not hide gaps by shrinking
scope. After this objective is achieved, return to Goal 0, reconcile the remaining
stages, and scaffold or resume Goal 2. No recursive goals or new corrective parent;
a completed child is not the parent's finish line.
```
