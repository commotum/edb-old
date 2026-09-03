# Goal 0 Continuation Prompt

```text
Run the parent execution loop for the native Rust Datomic-inspired database described in /home/jake/Developer/atomic/goal-0/0-plan.md, following /home/jake/Developer/atomic/goal-0/0-loop.md.

Preserve the semantics and key benefits expressed by datomic_pro_docs. Treat 1.0.7705 as the default architectural and algorithmic blueprint: study and map its class/namespace boundaries, representations, data flow, algorithms, caching, concurrency, and performance choices, and match them wherever they remain sound. Translate idiomatically into Rust and deviate only where documented semantics, PostgreSQL, safety, or a clearly better Rust design justifies it; record significant deviations. PostgreSQL is the only storage system, so use it directly and do not build generic backend portability. Exact JVM/Clojure API, existing-database, wire-format, or byte-format compatibility is not required, but those source representations remain important design evidence rather than material to discard.

Sync Goal 0 with the actual repository, child plans, and evidence. Find the first parent stage whose completion signal is not established. If its matching goal-N scaffold is missing, use $scaffold-goal to create it; if it exists, reconcile and resume it without overwriting it. Execute that child goal through its full exit condition, verify the risky boundaries with real evidence, update both plans, then return to Goal 0 and revise unfinished stages as new facts warrant. Repeat this scaffold-or-resume, execute, verify, fold-back cycle in dependency order. Keep one active child and never treat scaffold creation as stage completion.

Do not stop after scaffolding or after completing one child while another stage can safely proceed. When all stages are marked complete, verify the integrated Goal 0 success condition and reopen the owning child for any gap. Continue until the original objective is genuinely achieved; stop earlier only for a concrete blocker or required authority, and report uncertainty plainly.
```
