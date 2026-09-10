# Goal 0 implementation loop

1. Read `0-plan.md`, current `1-audit.md` findings for the active child, and its
   three scaffold files. Reconcile actual code/tests and user decisions. Do not
   restart the completed corpus audit or EDN implementation.
2. Select the first unfinished implementation stage (Stages 1–9 → Goals 2–10).
   Reconcile its existing scaffold; use scaffold-goal only if it is missing.
   Keep one active child and no recursive goal hierarchy.
3. Execute the child: identify the actual end-user behavior, consult local docs
   and recovered evidence, implement idiomatic native Rust over existing engines,
   and add permanent public-path regressions.
4. Validate semantics, exact values/identity/retries and relevant real
   PostgreSQL/application workflows. Measure complete costs where performance is
   claimed. Do not equate test discovery, skipped fixtures or knobs with evidence.
5. Record material decisions, results and status in the child and parent. Update
   audit dispositions with dated evidence without erasing the historical review.
6. When the child's completion signal holds, return here and continue the next
   unfinished stage. Final integration failures reopen the owning child.
7. Optional integrations and platform differences follow the plan's explicit
   decision table. Ask only when new authority or a material user choice is needed;
   continue unaffected native work meanwhile.
8. Finish only when the original required product objective and integrated
   acceptance hold. At session boundaries leave the exact active stage, verified
   results, remaining work and blockers. Never report scaffold creation as product
   completion.
