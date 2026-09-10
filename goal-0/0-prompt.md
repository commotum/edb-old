```text
Audit every file in /home/jake/Developer/atomic/datomic_pro_docs against Atomic's
current Rust implementation to identify remaining missing or partial functionality.
Follow /home/jake/Developer/atomic/goal-0/0-plan.md and
/home/jake/Developer/atomic/goal-0/0-loop.md; the per-file checklist and evidence
catalog are /home/jake/Developer/atomic/goal-0/1-audit.md.

Sync those records with actual docs/source, then continue the first unfinished
stage using best judgment and bounded parallel reviews when useful. If the initial
pass is complete, investigate unresolved catalog candidates or changed evidence
instead of restarting completed folders. Compare behavior and usable APIs, not
names alone. Distinguish confirmed gaps, partial
support, candidates, present Rust equivalents and platform differences; retain
precise document/code evidence and deduplicate findings. Goal1 in another session
owns EDN frontends: do not edit or duplicate it. Identify gaps without implementing
features or changing shared source/tests/data. Fold material findings and status
back into the audit and plan, leaving a clear continuation when interrupted.

Finish when every corpus file is accounted for and the remaining gap catalog is
reconciled, evidence-backed and actionable. Report uncertainty and the limits of
static review plainly; do not claim unrun tests or full Datomic conformance.
```
