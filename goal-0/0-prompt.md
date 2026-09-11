# Continue Goal 0

```text
Cut Atomic over to Datomic-style storage using
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.

Rust must own the log/index structures, publication, receipts, retention and
maintenance over opaque immutable blocks and conditionally updated root references.
PostgreSQL is the only storage provider, not the database-engine policy layer.
Use local datomic_pro_docs as semantic authority and 1.0.7705 as architectural
evidence; translate idiomatically, with no JVM parity or other backend project.

This is a full first-release cutover. Fresh formats/databases are allowed; no
old-format readers, upgrade/rollback converters, historical-executable matrix or
permanent dual-engine path. Preserve current user-facing capabilities and
current-version correctness, not existing SQL layouts. Remove superseded SQL,
code, dependencies, tests, fixtures and docs while porting useful regressions.
Do not reset unrelated databases or delete the Datomic reference corpora.

Run Goal 0 as the parent loop. Sync actual code/tests, select the first unfinished
stage and matching goal-N (Stages 1–7 -> Goals 1–7), resume existing work or use
$scaffold-goal for a missing child, then execute it through completion. Keep one
child active and no recursive/corrective parents. Use real PostgreSQL/application
checks, architectural checks and measured complete-path costs. Fold material
decisions/results into both plans, return to Goal 0 and continue through all stages.

Finish only when the full new-storage product, cleanup and integrated acceptance
hold, with no hidden legacy fallback or silent feature reduction. Reopen owning
stages for gaps; leave concise continuation notes and report blockers/uncertainty
plainly. A scaffold or completed child is not the parent's finish line.
```
