# Goal 8 execution loop

1. Sync `0-plan.md` with Goal 0's scope, current code/tests and relevant audit evidence.
2. Select the first unfinished internal stage; preserve existing repairs and APIs.
3. Implement the smallest coherent user-visible advance using idiomatic Rust and
   the existing engines. Read relevant original docs/source for semantic decisions.
4. Add permanent regressions; exercise public APIs, freshly created PostgreSQL
   databases and evolving application/EDN workflows in proportion to the risk.
   Cross-version migrations, old-binary fixtures and mixed-version upgrades are
   not acceptance gates. Reject unsupported formats clearly; never reset implicitly.
5. Check current-version immutable history, exact data/identity and retries
   (including after rebinding), restart/crash recovery, same-version failover,
   backup/restore, cancellation and isolation where relevant. Measure complete
   paths where costs inform the implementation or support a claim. Reuse unaffected
   passes; do not repeat full acceptance matrices or impose speculative hard bounds.
   Keep Rust/EDN capabilities; avoid unrelated compatibility/cosmetic cleanup.
6. Fold material decisions, results and status into this plan and Goal 0. Continue
   until the full child signal holds, then return to the parent's next stage.
7. Reopen gaps rather than narrow acceptance. At session boundaries record actual
   progress, next action and blockers. No recursive scaffolds or substitute parent.
