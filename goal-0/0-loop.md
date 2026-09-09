# Goal 0 Execution Loop

1. Read `0-plan.md` and reconcile its current stage with code and actual results.
   Archived G1/G2/G3 goals are evidence, not active instructions. Preserve proven
   repairs and distinguish completed core acceptance from this phase's missing
   product capabilities.
2. Select the first unfinished stage and its matching `goal-N/` (Stages1–7 map
   to Goals1–7). Use `$scaffold-goal` to create a missing child's three files;
   otherwise reconcile and resume its existing work. Keep only one child active.
3. Execute the child through its observable outcome. Consult local Datomic docs
   and relevant recovered source, implement the most direct coherent extension,
   and exercise the executable/application workflow throughout. Scaffolding is
   not completion; do not create child hierarchies or another corrective parent.
4. Verify the changed boundary with suitable semantic tests, actual PostgreSQL,
   deployment/security/failure checks and measurements. Reuse valid earlier
   evidence; broaden checks for concrete risks, not to accumulate green totals.
   Keep optional optimization metadata outside transaction meaning and preserve
   the documented fulltext freshness/visibility distinction.
   For the owning stages, verify cross-process wakeups with durable replay,
   exact retained snapshot handoff, plain-data/log query sources, and opt-in
   SSD cache behavior. Test native cache-resident reads without foreground
   storage calls while preserving GC/pin safety. Measure all SQL and background
   work, cold-miss isolation and increasing reader counts; local `db()` capture
   or zero node-read counters alone do not establish read independence.
5. Fold material decisions, results, limits and stage status into the child and
   parent plans. Never silently defer a required feature. If completing it would
   require a material scope change, report evidence and ask the user; do not
   manufacture completion by relabelling the feature optional.
   Runtime opt-in caches/hints still require implementation and measurements.
   Keep snapshot retention and consumer replay limitations explicit; do not
   promise infinite history or exactly-once external effects.
6. Once the child completes, return to Step1 and execute the next unfinished
   child. After the last child, check the whole original objective and integrated
   application; reopen the owning child for a remaining gap. Close Goal0 only
   when all required capabilities and integrated acceptance are established.
7. At an unavoidable session boundary leave a concise continuation note: active
   child, last verified result, concrete issue and next action. Report genuine
   blockers or uncertainty plainly; do not pause merely because a child or its
   scaffold is finished while safe in-scope work remains.
