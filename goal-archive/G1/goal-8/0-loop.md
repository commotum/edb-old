# Goal 8 Working Loop

1. Reconcile `goal-8/0-plan.md`, Goal 0, all completed child evidence, current
   migrations and the real PostgreSQL environment. Start with the first
   operational stage whose completion signal is not observed.
2. Trace the applicable docs and recovered backup/integrity/monitor/garbage/
   excise/index/lifecycle paths before fixing an important contract. Use the
   mapping to preserve intent, not JVM or multi-storage machinery.
3. Ship the smallest executable operator boundary and its failure proof. Favor
   a direct PostgreSQL transaction or native tool where it is safer than a new
   abstraction. Keep destructive paths privileged, targeted and reversible by
   a separately verified backup.
4. Test at the claimed boundary: raw-row corruption and inspection, measured
   capacity, external backup files, restore into a separate target, concurrent
   GC/publication, excision rollback/raw scans/cache refresh, and actual restart.
5. Record RPO/RTO assumptions, commands, metrics, destructive preconditions,
   significant deviations and observed results in the plan/runbooks. Never
   equate documentation or a same-cluster copy with demonstrated protection.
6. Continue through every stage. Do not stop after adding dashboards, a backup
   command, or an excision predicate while restore/privacy evidence is missing.
7. Run the full Goal 0 acceptance audit serially where shared PostgreSQL
   restart/corruption fixtures require it; strict formatting/lint must pass.
   Reopen the owning earlier goal if integration reveals a semantic gap.
8. On completion update Goal 0 Stage 8 and its success condition. If authority
   for an irreversible/environmental action is genuinely missing, record the
   blocker and exact safe next action; otherwise keep advancing useful behavior.
