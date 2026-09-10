# Goal 1 execution loop

1. Read this child's plan and Goal0's plan/loop. Sync with actual code, tests,
   existing repairs and current fixture availability. Keep R1/R2/R3 in scope;
   do not recreate the review or infer that old green tests prove them fixed.
2. Select the first unfinished internal step. Take the most direct useful action:
   reproduce a counterexample, repair the responsible shared path, or prove the
   change through a real application boundary. Parallelize only bounded work
   inside this child; create no child-of-child folders.
3. Check docs/recovered algorithms where meaning is uncertain. Preserve immutable
   values, valid query forms, canonical identity and old durable receipts. Resolve
   algorithm/version implications explicitly instead of lowering the requirement.
4. Validate independently expected results, nearby valid/invalid cases, actual PG
   execution and restart/retry as required. Distinguish skipped, blocked, failed
   and passed checks. Reuse permanent tests and existing application support.
5. Update this plan and the parent's Stage1 with material decisions, changed facts,
   verified results and outstanding work. Continue until step4's completion signal;
   no plan-writing, lint count or isolated witness alone completes the child.
6. At a session boundary, record the last verified state, current blocker if any,
   exact next action and fixture/authority needed. Keep the child resumable.
7. On child completion, return to `/home/jake/Developer/atomic/goal-0/0-loop.md`,
   reconcile all remaining stages and continue with the next unfinished child.
   Report the child's outcome honestly; do not call the parent product complete.
