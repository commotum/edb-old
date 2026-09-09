# Goal 0 Continuation Prompt

```text
Complete the next phase of the native Rust/PostgreSQL Datomic-inspired database
using /home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md.

Preserve the verified information model and existing implementation. Implement
the planned executable/admin product, small application APIs, secure cross-host
submission/discovery/reconnection, notifications and restartable log consumers,
shareable exact snapshot references and inexpensive logical snapshot/view
comparison, storage-independent cache-resident reads, an opt-in SSD cache,
complete I/O instrumentation and advisory transaction hints,
native block compression and batched index I/O, structurally shared/indexed
speculative values, richer persisted-program queries/authoring, prepared-query
reuse, set-oriented joins and plain-data/log
query sources, integrated peer-local fulltext, partition assignment, reproducible
generated failure testing, a safe functional planning workflow, and operating
acceptance.
Do not silently turn these required capabilities into scope exclusions.

Use /home/jake/Developer/atomic/datomic_pro_docs as semantic authority and
/home/jake/Developer/atomic/1.0.7705 as architectural/algorithmic evidence.
Use native Rust and PostgreSQL; do not recreate JVM/Clojure execution, Datomic
wire/storage compatibility, other stores or exhaustive decompilation parity.
Hints must not change transaction identity or results. Fulltext may lag, but
returned facts must respect the supplied database view. PostgreSQL remains the
sole durable authority; the SSD cache is disposable and optional to enable,
not optional to implement. Preserve GC/pin safety, lossless live reports, and
explicit snapshot-retention and consumer checkpoint/replay semantics.
Preserve old block formats/hashes and durable-before-publication ordering.
Prepared queries must not cache answers or stale source/schema bindings.
Snapshot keys identify logical views, not physical roots or just basis numbers;
document opaque-filter/speculative comparison limits. Preserve pure with and
stack/drop safety while measuring branch sharing, selective reads and memory.
Demonstrate one value-taking application calculation with native and fabricated
fixtures; then branch, compare, select and revalidate intent at commit. Preview
does not reserve a commit, speculative IDs are not durable allocations, and
sequential previews are not automatically one atomic transaction.

Run Goal0 as the parent loop. Sync the plan with actual code/tests, select the
first unfinished stage and its matching /home/jake/Developer/atomic/goal-N folder
(Stages1–7 map to Goals1–7). Use $scaffold-goal for a missing child's 0-plan.md,
0-loop.md and 0-prompt.md; otherwise reconcile and resume without replacing work.
Goal1 is already scaffolded. Execute each child to its completion signal, fold
material decisions/results into both plans, return to Goal0, and repeat through
the next unfinished child until the entire objective is established.

Use real PostgreSQL and exercise the actual executable/application workflow.
Measure increasing reader counts, cold/warm and mixed analytics workloads, and
all foreground/background storage traffic. Local db() capture or zero node-read
counters do not prove a storage-independent native query. Preserve applied
migrations, durable meaning, existing data and completed repairs.
Reuse existing differential/fault tests, record seeds and replay traces, and
reduce failures. Deeper transaction pipelining is a phase-measured decision,
not a required rewrite or permission for parallel transaction authority.
goal-archive/G1, G2 and G3 are historical evidence, not active instructions.
Keep one child active; no recursive hierarchy or corrective parent.
A scaffold or completed child is not the parent's finish
line. Reopen the owning child for an integrated gap. Leave concise continuation
notes at session boundaries and report blockers or uncertainty plainly.
```
