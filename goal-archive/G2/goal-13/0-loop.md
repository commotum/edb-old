# Goal 13 Working Loop

1. Reconcile `0-plan.md` with Goal 12's actual base/peer implementation and the
   latest real PostgreSQL evidence; select the first unfinished stage.
2. Read the exact `datomic_pro_docs` passages and recovered 1.0.7705
   node/seek/merge/connection code for that stage before selecting a native
   representation or algorithm.
3. Add the smallest measurable witness for the active eager or correctness
   gap. Use the pure `Database` as an independent oracle, not as the production
   implementation under test.
4. Implement one vertical slice through canonical nodes, direct PostgreSQL
   persistence, lazy access, cache/connection state, and publication as needed.
   Preserve old snapshots and the authoritative-log boundary throughout.
5. Measure node reads/writes, reused hashes, merge work, recent-tier size, and
   cache residency. Do not infer bounded/sublinear behavior from elapsed time
   or a synthetic status counter.
6. Run focused pure tests immediately and real PostgreSQL concurrency,
   corruption, restart, long-history, and cache-pressure tests for risky
   boundaries. A self-skipped test is not acceptance evidence.
7. Fold source anchors, significant Rust/PostgreSQL deviations, results,
   limitations, and truthful status into `0-plan.md`. Continue through the
   full exit condition.
8. At completion update Goal 9 and Goal 0, return to the corrective parent,
   scaffold Goal 14, and continue. If interrupted, leave the first failing
   witness and next state-changing action.

Prefer one working tree/seek/merge path with honest instrumentation over a
parallel abstraction, speculative distributed machinery, or benchmark theater.
