# Goal 0 execution loop

1. Read `0-plan.md`, sync its stage/component status with actual files and results,
   and preserve unrelated work. Archived goals are evidence only.
2. Select the first unfinished stage and a coherent component or documented
   behavior within it. Work from the source atlas so duplicate artifact copies
   do not become duplicate analysis. Do not create recursive goal hierarchies or
   new corrective parents; this scaffold is sufficient to begin work.
3. During source study, read the implementation and the important callers and
   dependencies. Add concise inline WHY notes, distinguishing observations,
   documented reasons, inference and unknowns. Advance real coverage, not comment
   volume. During doc mapping, trace important passages to implementing symbols,
   not merely a similarly named module.
4. Complete the source-understanding, doc-trace and architecture stages before
   production cutover. During porting, use this component loop:
   - Re-read the relevant source notes and documented contract.
   - Explain the original mechanism and identify the Rust representation that
     preserves it, its state/coordination boundaries and cost behavior.
   - Port or reuse the implementation in its new owner; update callers and trace
     links; remove the superseded path in the same component change.
   - Exercise the user-facing behavior and inspect the resulting architecture.
     Correct the notes when implementation work disproves an earlier inference.
5. Verify in proportion to the change: source/provenance and link checks for
   annotations, focused semantic cases for ports, real disposable PostgreSQL and
   application workflows for durable/process boundaries, measured full paths for
   performance claims. Use current-version tests, not a historical compatibility
   matrix. Green skipped storage tests are not storage evidence.
6. Fold material decisions, uncertainties, verified results and remaining work
   into the authoritative plan and existing trace records. Prefer a direct link
   to the evidence over another report. Parallelize disjoint source/components
   when useful, with one integration owner and no duplicate reviews.
7. Continue to the next component/stage. If integration exposes a gap, reopen the
   owning stage and repair it; do not narrow the objective to call the stage done.
   Stop for user direction only when a material new scope choice is necessary.
8. At a session boundary, replace the concise continuation note with the exact
   next action and any real blocker. Finish only after the annotated source,
   passage-level traces, source-derived Rust cutover, structured docs, cleanup
   and integrated acceptance all hold. Report uncertainty plainly.
