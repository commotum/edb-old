```text
Implement Atomic's block-native backup, offline reads, restore and administration
using /home/jake/Developer/atomic/goal-6/0-plan.md and
/home/jake/Developer/atomic/goal-6/0-loop.md as Stage 6 of Goal 0.
Use the local Datomic docs/source as semantic/architectural evidence. Reuse the
Rust value/log/index engine; PostgreSQL supplies opaque objects and guarded refs,
not engine policy. Preserve current capabilities, exact retries and route safety.
Fresh formats are allowed; no old readers, converters or hidden fallback.
Sync actual work, execute remaining stages with real PostgreSQL/application tests
and measured complete costs, remove displaced code and fold results into both
plans. No grandchildren. Finish only at this child's full observable acceptance,
report gaps plainly, then return to the parent's remaining work.
```
