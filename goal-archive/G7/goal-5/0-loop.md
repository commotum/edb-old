# Goal 5 — Execution loop

1. Read this plan and the parent; reconcile actual source/tests and retained work.
2. Execute the first unfinished internal stage without scaffolding grandchildren.
3. Use Rust-owned immutable structures and guarded roots; consult semantic docs at
   design decisions and reuse proven pure algorithms. Fix the next concrete gap.
4. Exercise isolated PostgreSQL/application checks and bounded complete-path costs,
   including ownership/publication/cleanup. Never claim skipped tests ran.
5. Port useful regressions, remove superseded implementations, and fold material
   decisions/results into this plan and Goal 0. Preserve unrelated data/corpora.
6. Continue until all stage outcomes hold; then return to Goal 0's next unfinished
   stage. At a session boundary leave a concise truthful continuation note.
