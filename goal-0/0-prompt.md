# Goal 0 Continuation Prompt

```text
Continue building the native Rust Datomic-inspired database described in /home/jake/Developer/atomic/goal-0/0-plan.md, following the working rhythm in /home/jake/Developer/atomic/goal-0/0-loop.md.

Preserve the semantics and key benefits expressed by datomic_pro_docs. Treat 1.0.7705 as the default architectural and algorithmic blueprint: study and map its class/namespace boundaries, representations, data flow, algorithms, caching, concurrency, and performance choices, and match them wherever they remain sound. Translate idiomatically into Rust and deviate only where documented semantics, PostgreSQL, safety, or a clearly better Rust design justifies it; record significant deviations. PostgreSQL is the only storage system, so use it directly and do not build generic backend portability. Exact JVM/Clojure API, existing-database, wire-format, or byte-format compatibility is not required, but those source representations remain important design evidence rather than material to discard.

Sync the plan with the actual repository and evidence, take the first unfinished stage toward its completion signal, verify the result proportionately, and fold material findings and status back into the plan so the goal remains resumable. Use best judgment on implementation details. Continue until the original objective is achieved; report genuine blockers and uncertainty plainly.
```
