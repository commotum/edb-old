# Goal 1 — Execution loop

1. Read this plan and Goal 0; reconcile with actual code and test results.
2. Execute the first unfinished internal stage. Keep the boundary opaque and
   policy in Rust; consult relevant local docs/source when making design choices.
3. Reuse working codecs and diagnostics. Implement the actual PostgreSQL path,
   not an in-memory substitute or a facade over old feature tables.
4. Run focused permanent unit and fresh-schema PostgreSQL checks. Test rejection,
   races and reopen; measure complete paths without claiming unmeasured scale.
5. Record material decisions/results and remaining gaps in this plan and Goal 0.
   Continue internal stages until the observable completion signal holds.
6. Return to Goal 0 and execute the next child. Do not create grandchildren or
   declare the parent complete because this storage foundation passes.
7. At a session boundary leave a concise next action and concrete blocker, if any.
