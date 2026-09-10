# Goal 0 execution loop

1. Read `0-plan.md` and reconcile the checklist in `1-audit.md` with current docs,
   source and Goal1's externally owned EDN work. Preserve other sessions' changes.
2. Select the first unfinished folder or its unresolved review entry. Read its
   document and relevant API/implementation paths, rather than searching only
   for matching names. Use disjoint parallel reviews where useful.
3. Identify user-visible requirements and map them to actual behavior. Inspect
   relevant tests and supported examples to challenge suspected gaps.
4. Record precise evidence and disposition. Confirm important negative claims
   across public interfaces and alternatives; leave uncertain behavior as a
   candidate instead of calling it absent. Do not claim unexecuted tests passed.
5. Merge duplicate findings, keep EDN with Goal1, and update file coverage, material
   conclusions and stage status in the audit and plan. Keep records useful and
   concise; add no extra planning machinery.
6. Continue through all folders and final synthesis. This is an identification
   goal; no product implementation, shared fixture mutation or new child goal is
   needed without further instruction.
7. At a session boundary, record reviewed files, strongest findings, unresolved
   question and next action. Completion means the corpus and gap catalog have
   been reviewed, not that Atomic implements every documented feature.
