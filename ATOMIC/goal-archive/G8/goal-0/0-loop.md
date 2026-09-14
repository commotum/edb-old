# Goal 0 execution loop

1. Read 0-plan.md and sync stage/component status with actual code, source notes,
   docs and results. Preserve unrelated work. Reuse applicable evidence; do not
   rerun whole campaigns merely because a new session started. Archived goals
   are evidence only.
2. Select the first unfinished stage and one coherent user-visible behavior or
   component. Use the atlas to find its actual source, callers and dependencies.
   Artifact membership, missing type names and dependency presence are not
   feature-gap evidence. Keep one integration owner; parallelize only bounded,
   disjoint work that advances this component or removes a real blocker.
3. Before changing that component:

   - Read the relevant source bodies and consumers, including dependencies/Java
     where they supply the mechanism.
   - Add concise inline ATOMIC-NOTE rationale and trace the relevant Pro-doc
     passages. Separate fact, documented rationale, inference and unknown.
   - Compare the existing Rust invariants, ownership, coordination, failure and
     cost behavior. Classify findings as behavior gaps, architectural mismatches,
     optimizations or packaging differences.
   - Choose retain, move, adapt, replace or remove, with a concrete reason.
     Settle the component owner and suitable module/crate boundary.

4. Implement or retain the component accordingly. Update callers, examples and
   trace links, and remove the superseded path in the same bounded cutover.
   Prefer reuse and composition; no mandatory priority-map port, crate per
   namespace, generic-framework project or rewrite of proven equivalents.
   Incomplete annotation of unrelated components does not block this change.
5. Verify the outcome, not activity:

   - Check source/provenance and links for annotation-only changes.
   - Use focused permanent semantic/structural regressions for changed behavior.
     Shared-engine differential tests establish consistency, not independent
     proof of Datomic equivalence; use documented examples and independent
     expected results for the intended contract.
   - Exercise the real application and disposable PostgreSQL at changed durable,
     process or operational boundaries. Missing configuration is a skip, never
     acceptance. Inspect permission/environment failures before labeling them
     product bugs; request permitted reruns where appropriate, without weakening
     security checks or disguising the original failure.
   - For performance claims, compare complete paths and relevant sizes, including
     setup, read/write work, allocation/retention and contention as appropriate.
     Structural bounds and counters are not automatically latency or RSS proof.
   - Inspect dependency/state flow separately from output correctness.

6. Fold material decisions, source/doc/Rust links, verified results and remaining
   gaps into 0-plan.md and existing trace records. Correct explanations when
   evidence changes. Do not create duplicate reports, completion ceremonies,
   recursive child goals or another corrective parent.
7. Complete the stage's observable outcome, then continue to the next unfinished
   stage. Reopen the owning stage for integrated or coverage-discovered gaps.
   Global corpus and passage coverage remain required, but are accumulated
   component by component, not used as a blanket gate before implementation.
8. At session boundaries leave a concise next action and real blockers. Finish
   only when all stages, source learning, doc traces, organized product, cleanup
   and integrated acceptance hold. Report unverified claims and uncertainty
   plainly; stop for direction if a material new scope choice is required.
