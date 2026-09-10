# Goal 7 continuation prompt

```text
Implement Atomic's reliable stock services and clients stage using
/home/jake/Developer/atomic/goal-7/0-plan.md and
/home/jake/Developer/atomic/goal-7/0-loop.md.
This is Stage 6 of /home/jake/Developer/atomic/goal-0/0-plan.md, owning AO04, AO05, AO-C01, P04, AO07; AO-C03 disposition.

Use local Datomic Pro docs for semantics and 1.0.7705 for architectural evidence.
Deliver useful idiomatic Rust equivalents in spirit, not literal JVM/Clojure
machinery. Follow Goal 0's fresh-database policy: schema/format changes may require
a newly created database; cross-version migration, old-binary fixtures and
mixed-version rolling upgrades are not acceptance gates. Reject unsupported
formats clearly without silent reinterpretation or automatic resets.
Preserve current-version data integrity, immutable history, exact identity/schema,
restart/crash recovery, same-version failover, current-version backup/restore and
receipt-first exact retries, including after rebinding in a supported database.
Keep cancellation, isolation and complete-path performance requirements. Existing
Rust/EDN API scope remains; do not turn this into compatibility-code cleanup.
PostgreSQL remains storage; exercise new-format behavior in fresh databases.
Apply Goal0's proportionate-implementation policy: reuse unaffected evidence,
focused regressions and representative integration; no duplicate matrices,
invented hard bounds or completion ceremonies. Optional unapproved work is
deferred, not a blocker. Required capabilities and current-version safety remain.
Sync actual code/tests, execute the first unfinished internal stage, verify public
application/PostgreSQL paths and measured complete costs, and fold results into
child/parent plans. Do not create recursive goals or silently change scope.
Finish at this child's observable completion signal, report gaps plainly, then
return to Goal 0's next unfinished stage. Leave a concise continuation if interrupted.
```
