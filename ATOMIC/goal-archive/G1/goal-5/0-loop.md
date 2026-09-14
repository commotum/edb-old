# Goal 5 Working Loop

1. Read `goal-5/0-plan.md`, Goal 4's completed peer/index contract, and current
   code/tests; reconcile status with executable behavior.
2. Select the first unfinished stage and implement the smallest complete
   query/pull/API behavior that advances its observable completion signal.
3. Read the relevant `datomic_pro_docs/05_query_and_pull` or peer API section,
   then trace only the corresponding `1.0.7705` `datalog`, `query`, support, or
   pull path before choosing representation, scope, or algorithm.
4. Keep a simple evaluator as the semantic oracle. Add index selection,
   planning, caching, or specialization only behind differential evidence.
5. Test reads against one immutable snapshot and all applicable temporal views.
   Exercise cancellation/resource bounds and concurrency when those boundaries
   are introduced; use real PostgreSQL only for end-to-end peer submission and
   synchronization, not for local query evaluation.
6. Record material recovered mappings, semantic decisions, deviations,
   unsupported forms, measurements, and truthful stage status in the smallest
   useful artifact.
7. Continue through the exit condition. Do not count AST types, parsers,
   examples, or a basic pattern scan as completion of Datalog, pull, or the
   native API.
8. If genuinely blocked, leave the exact unsupported semantic boundary and
   next state-changing implementation/test in the plan.

Prefer shipped query behavior and differential proof over framework layers.
Do not build Goal 6 function deployment, Goal 7 services, or Goal 8 operations
to make this goal look comprehensive.
