# Goal 3 — Execution loop

1. Reconcile this plan with actual code, tests and Goal 0's architecture.
2. Execute the first unfinished internal stage; reuse selective value/transaction
   components and take the next implementation step, not another inventory.
3. Verify real PostgreSQL/application behavior and ownership: opaque storage only,
   Rust receipts/log/publication, exact retry before mutable-state validation.
4. Exercise failures and measure complete paths. Correct core gaps instead of
   excluding them. Transfer useful regressions and remove displaced implementation.
5. Fold material results/status into this plan and Goal 0. Keep one active child,
   no recursive scaffolds, and leave a concrete continuation note at boundaries.
6. When all child signals hold, return to Goal 0 and continue Stage 4. Do not call
   a writer fixture, scaffold or this child completion the full product cutover.
