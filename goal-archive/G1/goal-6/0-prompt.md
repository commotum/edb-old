# Goal 6 Continuation Prompt

```text
Continue Goal 6, deterministic controlled programmability for the native Rust database, using /home/jake/Developer/atomic/goal-6/0-plan.md and /home/jake/Developer/atomic/goal-6/0-loop.md.

Treat datomic_pro_docs as semantic authority, completed Goal 1–5 contracts/tests as executable boundaries, and 1.0.7705 datomic.function, datomic.extensions, transaction expansion, query expression resolution, and cancellation paths as the default blueprint. Preserve db-before transaction-function evaluation, complete generated-data validation, immutable query snapshot locality, explicit version identity, deterministic execution and structured errors. Use Rust and PostgreSQL only. Persist only a versioned, statically validated, content-addressed, metered program format with no filesystem, network, SQL, environment, clock, randomness, threads, or unrestricted host calls; keep process-local Rust extensions visibly separate.

Reconcile the plan with actual artifacts, execute the first unfinished stage, verify interpreter and output semantics differentially, and use real PostgreSQL for deployment/activation/restart/concurrency/corruption evidence. Do not add generic storage, a broad language runtime, HA service machinery, or lifecycle operations. Continue until persisted functions reproduce across nodes/restarts, every limit fails closed, and generated behavior cannot bypass existing transaction/query validation; report genuine blockers plainly.
```
