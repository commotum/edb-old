```text
Continue Goal 12, the one-transactor and controlled-behavior correction for the native Rust database, using /home/jake/Developer/atomic/goal-12/0-plan.md and /home/jake/Developer/atomic/goal-12/0-loop.md.

Treat datomic_pro_docs as semantic authority, Goals 10/11 as the executable kernel boundary, and 1.0.7705 as the default blueprint for submission, serialization, transaction time, ownership, acknowledgement, functions/predicates, and base-plus-tail recovery. Keep Rust throughout and PostgreSQL only. Preserve Database::with as a pure transition; make one database-bound fenced transactor the only ordinary write path, make expected basis optional rather than mandatory, assign valid time at the server, retain durable idempotency and honest unknown outcomes, add only sufficient deterministic controlled behavior, and recover from a verified base plus log tail.

Sync the plan with current code and real PostgreSQL evidence, advance the first unfinished stage with working behavior, verify risky publication/failover/restart boundaries without counting self-skips, and fold material findings and status back into the plan so work remains resumable. Avoid generic storage, protocol ceremony, a general-purpose language runtime, or Goal 13 persistent-tree work. Continue through the full exit condition; report genuine blockers or uncertainty plainly.
```
