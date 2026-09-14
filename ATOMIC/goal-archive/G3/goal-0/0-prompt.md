# Goal 0 Continuation Prompt

```text
Finish the existing native Rust/PostgreSQL Datomic-inspired database using
/home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.

Preserve Datomic's central information model: immutable database values and
facts, declarative serialized transactions, strong identity, schema as data,
history/time views, peer-local query and navigation, and reliable operations.
Use /home/jake/Developer/atomic/datomic_pro_docs as semantic authority and
/home/jake/Developer/atomic/1.0.7705 as architectural and algorithmic evidence.
Use Rust and PostgreSQL directly; JVM/wire compatibility, other stores, and
exhaustive decompilation equivalence are not objectives.

Run Goal 0 as the parent loop. Sync its plan with actual code and tests, select
the first unfinished stage, and use its matching /home/jake/Developer/atomic/goal-N
folder (Stages 1–6 map to Goals 1–6). Use $scaffold-goal to create a missing
child's 0-plan.md, 0-loop.md, and 0-prompt.md; otherwise reconcile and resume
the existing child without replacing its work. Goal 1 is already scaffolded.

Execute the child through its observable completion signal using best judgment.
Preserve completed repairs, finish partial implementations, and exercise the
application workflow throughout. Prove PostgreSQL checks run and measure
claimed scalability. Archived A1/A2 goals are evidence, not active instructions.

Fold material decisions, verified results, and status into the child and parent
plans. Return to Goal 0, reconcile the remaining stages, then scaffold or resume
and execute the next unfinished child. Repeat until the original objective and
integrated acceptance are established; a scaffold or completed child is not
the parent's finish line. Reopen the owning child if final checks reveal a gap.
Keep one child active, leave a concise continuation note at session boundaries,
and report blockers or uncertainty plainly without hiding core gaps as scope
exclusions. Do not create recursive goal hierarchies or another corrective parent.
```
