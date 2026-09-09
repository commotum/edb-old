```text
Continue Goal 16, the tiered production transactor for the native Rust
Datomic-inspired database, using
/home/jake/Developer/atomic/goal-16/0-plan.md and
/home/jake/Developer/atomic/goal-16/0-loop.md.

Treat datomic_pro_docs as semantic authority, Goals 10–15 as executable
boundaries, Goal 9's evidence ledger as the corrective map, and 1.0.7705 as the
default blueprint for Db tiers, transaction assessment, index access, memory
handoff, log-tail recovery, and writer lifecycle. Use Rust and PostgreSQL
directly. Keep Database::with as the eager semantic oracle, but remove eager
Database ownership and whole-history reconstruction from every production
writer, report, restart, and failover path.

Sync the plan with current code and measurements, advance the first unfinished
stage, and build one exact lazy semantic access path for transaction expansion,
complete successor validation, persisted behavior, incremental commitments,
and publication. Preserve the fenced database-bound service, declarative
unordered transactions, identity, schema-as-information, history/time views,
acknowledgement/idempotency, immutable roots, and root-plus-tail recovery.
Prove semantics by eager/native differential tests and boundedness by explicit
resident-byte, decoded-range, commitment-work, long-history, consolidation,
restart, and failover evidence that actually executes PostgreSQL.

Do not add storage portability, a JVM/Clojure runtime, generic HA ceremony, or
Goal 17's broad deployment harness. Do not accept a compatibility materialize
fallback or a renamed full database as completion. Fold material source
findings, Rust/PostgreSQL deviations, and truthful status into the plan, then
continue until Goal 16's exit condition is genuinely established or a concrete
blocker requires outside authority.
```
