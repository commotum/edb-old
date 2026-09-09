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
Document actual cached-query storage dependence and I/O-counter scope; local
db() capture is not proof of offline queries, and live lossless report queues
are not durable restartable consumers. Reuse existing configuration policy;
coordinate the parent's later snapshot, replay, cache and query-source work
without requiring those implementations to close this local-deployment child.

Record material decisions, verified results and status in the child and parent
plans. Keep one child active and leave a concise continuation note at session
boundaries. Report blockers and uncertainty plainly; do not hide gaps by shrinking
scope. After this objective is achieved, return to Goal 0, reconcile the remaining
stages, and scaffold or resume Goal 2. No recursive goals or new corrective parent;
a completed child is not the parent's finish line.
```
