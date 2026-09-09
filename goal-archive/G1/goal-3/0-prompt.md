# Goal 3 Continuation Prompt

```text
Continue Goal 3, PostgreSQL durability and recovery for the native Rust kernel, using /home/jake/Developer/atomic/goal-3/0-plan.md and /home/jake/Developer/atomic/goal-3/0-loop.md.

Treat datomic_pro_docs as semantic authority, the completed Goal 1/2 contracts and tests as the executable kernel boundary, and 1.0.7705 as the default blueprint for log, immutable-value, SQL, conditional-root-publication, and recovery structure. Use PostgreSQL only and Rust throughout. Preserve Database::with as a pure transition; add explicit versioned canonical encoding, one concrete SQL schema, immutable transaction history, expected-basis atomic publication, durable idempotency, corruption detection, and exact restart recovery.

Sync the plan with current artifacts and real PostgreSQL evidence, advance the first unfinished stage with working behavior, verify risky boundaries using integration, concurrency, restart, and fault-injection tests, and fold material decisions and truthful status back into the plan. Do not introduce generic storage abstraction, peer/query work, background production indexes, HA leadership, or service ceremony. Continue until acknowledged commits survive, failed publications remain invisible, ambiguous retries cannot double-commit, and recovery reproduces the exact committed kernel state; report genuine blockers or uncertainty plainly.
```
