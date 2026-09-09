# Goal 11 Working Loop

1. Reconcile this plan with Goal 10's identity boundary, C03 in the corrective
   ledger, current code, and real test output; choose the first unfinished
   stage.
2. Read the exact documentation and recovered bootstrap/hook implementation
   before changing a system ID, schema property, or transaction timing rule.
3. Add the smallest executable witness for the missing information behavior,
   then implement one authority end to end through kernel, query, encoding, and
   recovery.
4. Remove or demote the corresponding side channel in the same stage. Never
   make typed schema and datoms independently mutable sources of truth.
5. Run focused pure tests immediately; run PostgreSQL recovery whenever the
   encoded/log/base boundary changes. A skipped integration test is not
   completion evidence.
6. Record material deviations and status here. At the exit condition, update
   Goal 9 and Goal 0, return to the corrective parent, scaffold Goal 12, and
   continue.
7. If interrupted, leave a passing/failing fixture and one concrete next action;
   do not substitute a vocabulary document for working database information.
