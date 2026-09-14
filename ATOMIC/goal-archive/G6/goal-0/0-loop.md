# Goal 0 implementation loop

1. Read `0-plan.md`, current `1-audit.md` findings for the active child, and its
   three scaffold files. Reconcile actual code/tests and user decisions. Do not
   restart the completed corpus audit or EDN implementation.
   Apply the parent's fresh-database policy ahead of older compatibility language:
   historical upgrades and old-binary matrices are no longer required.
2. Select the first unfinished implementation stage (Stages 1–9 → Goals 2–10).
   Reconcile its existing scaffold; use scaffold-goal only if it is missing.
   Keep one active child and no recursive goal hierarchy.
3. Execute the child: identify the actual end-user behavior, consult local docs
   and recovered evidence, implement idiomatic native Rust over existing engines,
   and add permanent public-path regressions.
4. Validate semantics, exact values/identity/retries and relevant real
   PostgreSQL/application workflows using fresh current-version fixtures. Exercise
   their restart, crash recovery, same-version failover and backup/restore; preserve
   history and receipts within that version. Reject unsupported formats explicitly.
   Measure complete costs where performance is claimed. Do not equate test
   discovery, skipped fixtures or knobs with evidence.
   Apply the proportionate-implementation policy: focused checks during editing,
   relevant integration once settled, reuse unaffected passes. No repeated full
   matrices, automatic benchmark ladders or hard bounds beyond the actual need.
5. Keep detailed decisions/results in the owning child and a short status/link in
   the parent. Update the audit only when an item's disposition changes; do not
   duplicate evidence logs or refresh unchanged scaffold prose.
6. When the child's completion signal holds, return here and continue the next
   unfinished stage. Final integration failures reopen the owning child.
7. Optional integrations and platform differences follow the plan's explicit
   decision table. Ask only when new authority or a material user choice is needed;
   continue unaffected native work meanwhile.
   Unapproved optional work is deferred, not a required-completion blocker.
8. Finish only when the original required product objective and integrated
   acceptance hold. At session boundaries leave the exact active stage, verified
   results, remaining work and blockers. Never report scaffold creation as product
   completion.
