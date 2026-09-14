# Goal 1 execution loop

1. Read `0-plan.md` and reconcile it with current code, relevant local Datomic
   docs, tests and any new Goal 0 handoff. Preserve existing work. The parent may
   own broader sequencing, but these internal stages need no new goal folders.
2. Select the first unfinished stage. Inspect only the evidence needed for the
   next consequential implementation decision; do not restart an exhaustive
   module review or treat archived goals as instructions.
3. Implement the smallest coherent change advancing that outcome. Reuse existing
   semantic engines and native APIs. Keep generic EDN reading distinct from
   schema-aware transaction/query/pull conversion and from durable serialization.
4. Add permanent regressions alongside each change. Exercise public APIs early;
   once transaction integration exists, use a disposable real PostgreSQL database
   and normal transactor/application paths throughout the remaining stages.
5. Check the important invariants: exact values, correct immutable basis,
   anonymous/explicit identity, nested ownership, schema ambiguity, no code
   evaluation, bounded malformed input and receipt-first exact retries. Include
   old typed requests, not only newly generated EDN receipts. Compare typed and
   EDN paths without requiring representation-dependent IDs/hashes to match where
   the contract does not promise that.
6. Record material decisions, commands/results, measured costs and unfinished
   acceptance in `0-plan.md`, and update the parent when present. Report failures,
   environment limitations and unexecuted PostgreSQL checks explicitly; do not
   substitute test discovery or parser-only success for integrated evidence.
7. Continue through this goal's remaining stages. Reopen an earlier stage if
   integration exposes a gap. At session boundaries leave a short continuation
   note with actual state, next action and any blocker. Stop for missing authority
   or a material scope decision rather than silently dropping requirements.
8. Finish only after the complete EDN application scenario and compatibility
   criteria hold. Hand verified results and honest remaining uncertainties back
   to Goal 0; this child's completion is not the parent's finish line.

Current state: all four EDN stages accepted 2026-09-10. The integrated raw-relation
gap was repaired and the unchanged CLI scenario passed on actual PostgreSQL.
Results, commands, measured costs and explicit native-domain gaps are in the plan.
Do not restart implementation without a new gap; return the handoff to Goal 0's
independently owned documentation audit. Preserve its files/status and do not
infer broader Datomic capability completion from this child's acceptance.
