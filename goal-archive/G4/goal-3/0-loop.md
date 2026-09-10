# Goal 3 Execution Loop

1. Reconcile this plan and parent Stage3 against actual code/results; preserve work.
2. Select the first unfinished stage and its smallest coherent implementation.
3. Use local semantic/algorithmic evidence and existing application/test support.
   Keep one child active; independent bounded implementation can run in parallel.
4. Verify semantics, durable compatibility, real PostgreSQL and measured resource
   costs at changed boundaries. Reuse generated/replay/reduction support as useful.
5. Fold material results, decisions, limits and status into this plan and Goal0.
6. Continue through every completion signal, then return to Goal0 and execute its
   next unfinished child. Reopen the owning child for an integrated gap.
7. At an unavoidable session boundary record last verified result, remaining issue
   and next concrete action. Do not silently exclude scope or add recursive goals.
