# Goal 1 continuation prompt

Status: EDN implementation accepted 2026-09-10. Read the plan's evidence/handoff
first; reopen only for a concrete regression or newly authorized requirement.
The independently owned Goal 0 is a documentation audit, not an implementation
loop to start automatically from this prompt.

```text
Implement Atomic's native EDN data frontend using
/home/jake/Developer/atomic/goal-1/0-plan.md and
/home/jake/Developer/atomic/goal-1/0-loop.md.

Deliver standard EDN reading/writing, entity-map and primitive transaction input,
query/input/rule and pull-pattern adapters, readable results, and a usable
file/stdin application path over the existing Rust/PostgreSQL product. Preserve
typed APIs, exact values, immutable database semantics, schema/identity behavior,
canonical durable data, prior repairs and receipt-first exact retries. Do not
execute Clojure/JVM code or build a replacement engine or alternate store.

Use local datomic_pro_docs as semantic authority, the EDN specification for the
format, and 1.0.7705 as architectural evidence. Reconcile Goal 0 when available;
this is its EDN child, not another parent. Archived goals are evidence only.
Sync actual code/tests, execute the first unfinished internal stage and continue
with best judgment. Do not create recursive goal scaffolds. Fold material
decisions, verified results and a concise continuation note into this plan and
the parent. Use permanent regressions, actual PostgreSQL/application checks and
measured complete-path costs. Finish only when the full EDN workflow and
compatibility acceptance hold; report blockers and gaps plainly, then return
control to the parent's remaining work.
```
