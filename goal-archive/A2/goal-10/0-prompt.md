# Goal 10 Continuation Prompt

```text
Continue Goal 10, kernel identity and valid-state repair, using /home/jake/Developer/atomic/goal-10/0-plan.md and /home/jake/Developer/atomic/goal-10/0-loop.md. Its parent is /home/jake/Developer/atomic/goal-9/0-plan.md and the verified source basis is /home/jake/Developer/atomic/goal-9/EVIDENCE_LEDGER.md.

Treat datomic_pro_docs as semantic authority and 1.0.7705's datomic.db make-eid/t->tx/get-ids/full-information assessment/tuple validation/typed ordering as the default blueprint. Keep Rust, PostgreSQL-only persistence, pure unordered Database::with, db-before resolution timing, immutable values, and explicit versioned formats. Do not implement general schema-as-data yet; leave that for Goal 11.

Resume the first unfinished stage, drive it with executable failure witnesses, propagate identity and invariant changes through encoding/index/query/storage/recovery, and verify all risky boundaries. Fold material results into the plan. Complete only when real PostgreSQL recovery as well as pure tests prove the exit condition; otherwise report the concrete blocker and next action.
```
