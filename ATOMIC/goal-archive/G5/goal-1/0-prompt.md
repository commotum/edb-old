```text
Complete Stage1 of the native Rust/PostgreSQL Datomic repair plan using
/home/jake/Developer/atomic/goal-1/0-plan.md and
/home/jake/Developer/atomic/goal-1/0-loop.md, under the parent
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.

Repair R1 negative-rule correctness, R3 disjunction readiness, and R2 anonymous
tempid collisions. Use the Datomic docs as semantic authority and recovered
1.0.7705 source as algorithmic evidence. Preserve valid native behavior, immutable
values, existing repairs, persisted data and exact old request receipts. No JVM
parity, blanket feature restrictions or silent reinterpretation of old data.

Sync current code/tests, work through the child with best judgment, add permanent
counterexample regressions, and prove the shared repairs through actual application
and PostgreSQL paths including restart/retry. Fold material decisions/results into
child and parent plans; keep one child active and leave concise continuation notes.
After this child's observable completion, return to Goal0 and execute the next
unfinished stage, using $scaffold-goal for a missing child's three files. Continue
the parent loop toward all repairs and integrated acceptance; do not create
grandchildren/corrective parents or treat this child as the parent's finish line.
Report blockers, skipped checks and uncertainty plainly.
```
