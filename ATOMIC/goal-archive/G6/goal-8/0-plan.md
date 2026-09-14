# Goal 8 — Actionable diagnostics and operational signals

## Objective and ownership

Users can explain expensive queries/transactions and operate the product using correlated, named metrics and structured events.

This is Stage 7 of [Goal 0](../goal-0/0-plan.md), owning D01, D02, D03, AO06, Q06.
Status: complete (2026-09-10). Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Goal 0's fresh-database
policy governs acceptance: schema/format changes may require a newly created
database. Cross-version database/format migrations, old-binary fixtures and
mixed-version rolling upgrades are not required. Reject unsupported formats
clearly; never silently reinterpret them or reset a database automatically.
Preserve current-version data integrity, exact identity/schema/values, immutable
history, restart/crash recovery, same-version failover, current-version backup/
restore and receipt-first exact retries, including after function rebinding in a
supported database. Retain cancellation, isolation and complete-path performance
requirements. This changes database-version support, not the existing Rust/EDN API
scope, and does not create a cleanup task to remove working compatibility code.
No JVM evaluation, replacement engine, alternate durable store or recursive goals.
Parent decisions govern optional scope.

Apply Goal0's proportionate-implementation policy: reuse unaffected evidence and
the existing application, use focused regressions and relevant integration, and
measure only useful complete paths. No duplicate build/transport/failure matrices,
invented absolute bounds or literal component parity. Required capabilities and
current-version safety remain; unapproved optional work is deferred, not a blocker.

Extend the existing attribution system with per-operation cache/index work, stable clause/binding/phase identities and transaction-correlated semantic counters. Add bounded configurable publication/logging and meaningful alarms. Retain the native optimizer by default; explain actual scheduling rather than copy Clojure tuning rituals.

Alarms mean configurable structured warning/error events usable by existing
monitoring, not a new alerting server/dashboard/backend. Clause identities need
unambiguous correlation within a query/execution, not cross-release stability.
Cover actionable native metrics, not Datomic's exact names or every vendor counter.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Reproduce uncertain behavior; use existing evidence for obvious API/integration
gaps. Record only consequential decisions here.
**Completion signal:** Required behavior, native design, API and current-version integrity risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Users can explain expensive queries/transactions and operate the product using correlated, named metrics and structured events.
**Focus:** Extend the existing attribution system with per-operation cache/index work, stable clause/binding/phase identities and transaction-correlated semantic counters. Add bounded configurable publication/logging and meaningful alarms. Retain the native optimizer by default; explain actual scheduling rather than copy Clojure tuning rituals.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified current-version operation and recovery.
**Focus:** Evolve application examples and EDN paths where relevant; use freshly
created PostgreSQL databases and ordinary service/peer boundaries. Verify exact
retries, immutable history, restart/crash recovery, same-version failover and
current-version backup/restore where relevant; measure complete costs. Historical
format and old-binary upgrade fixtures are not acceptance gates. Reopen
implementation for integration gaps.
**Completion signal:** Concurrent cached/uncached reads, nested queries, upserts/program transactions and indexing/failover produce attributable reports without cross-operation contamination or payload/secret leakage. Stock service emits verified events/counters; disabled instrumentation has measured costs and no semantic effects.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
All three internal stages are complete. Return to Goal0; Goal9 is the next child.

## Accepted native behavior and evidence

- D01: opt-in named OperationContext reports extend existing SQL/phase attribution
  with actual decoded/disk/PostgreSQL/in-flight cache reads and index-sort work.
  Named descendants aggregate inclusively, capped at128 labels; unnamed service
  submissions inherit the nearest caller label. No concurrent global subtraction.
  `measure` preserves the original result; lazy work stays with captured contexts.
  Query/Pull CLI `--io-context` and `io_report_to_edn` expose readable reports.
- D02/Q06: optional query clause paths, start/schedule order, binding sets,
  nested phases, work/row flow and warnings explain the existing native optimizer.
  Capture limits truncate metadata, not results or semantic budgets. Typed/EDN,
  `--query-stats`, prepared queries and QuerySequence share the same contract.
- D03: actual eager/native identity/upsert/uniqueness/redundancy/composite/function
  work is correlated with transaction basis and replay status. Receipt-first
  retries perform no new assessment; stats are ephemeral, not new durable/wire
  content. Remote users correlate server events by basis, not fabricated old stats.
- AO06: optional bounded JSON publisher and callbacks integrate with existing
  service/lifecycle loops; configurable operational warnings need no new backend.
  Producers never invoke sink code. Queue/event limits drop diagnostics only;
  shutdown can time out a blocked sink. Stock startup/status stdout is synchronous
  and must be drained; final STOPPED is omitted after a sink shutdown timeout.
  Names are intentional metadata, never a place for secrets or subject values.
- Fixed two integration defects: SQL permission/operational errors no longer
  masquerade as corrupt manifest content; unrelated cache-load failures are not
  attributed to the caller waiting behind them. Known commits remain confirmed.

Focused current-version acceptance (debug builds, offline dependencies, isolated
local PostgreSQL on port55471 with durable settings; zero skips/ignored):

| Witness | Verified result |
| --- | --- |
| query_diagnostics |4 pure +1 actual PG pass; nested phases, caps, privacy, unchanged results/work |
| transaction_diagnostics |2 pure +1 actual PG pass; inherited labels, native functions, commit/restart/rebinding/exact retry; final PG2.65s |
| telemetry |7 pass including1 actual PG; blocked sink, fresh/replayed commits, valid JSON, drop/error accounting, EDN exact counts;2.10s |
| sql_attribution |13 pass including4 actual PG; concurrent/nested contexts, bounded labels, lazy streams, errors/rollback;0.04s |
| native_ssd_cache |1 actual PG pass; cold/disk/decoded attribution, corruption fallback, disabled behavior and purge; final10.90s |
| private unrelated-miss regression |1 actual PG pass in disposable dedicated schema;1.54s; schema removed after test |
| remote_routing_edges |1 restricted PG/TLS pass; report error retains Forbidden/42501, known commit and exact retry;3.13s |
| product_cli |3 pass including separate stock service/application across2 rounds/restarts with telemetry, plus data-only EDN diagnostics;9.40s |
| remote_product stock auto contender |1 restricted PG/TLS process-crash/takeover pass with waiting/active events, actual lease epochs, retries/index/excision/held reads;6.33s |

Build atomic/application_workflow before CLI witnesses. Reproduction uses
`CARGO_PROFILE_DEV_DEBUG=0 CARGO_PROFILE_TEST_DEBUG=0 CARGO_INCREMENTAL=0`,
`cargo test --offline -j4 --test <target> -- --nocapture --test-threads=1`
and the scoped `ATOMIC_POSTGRES_URL`; the remote_product witness uses its exact
stock-auto filter, not the unrelated whole matrix. `cargo check --all-targets`
passed; Clippy lib/bins/examples passed with existing warnings (one new local
style warning fixed). `git diff --check` passed. Reuse unaffected Goal7 coverage.

Representative measurements, not throughput/zero-overhead claims:8 complete
query calls14.864ms disabled/14.779ms enabled with identical1018 work units each;
16 complete speculative calls0.952s/0.931s. Final warm native read875µs enabled/
834µs disabled; cold PG payload7855 bytes, restart disk payload8122 bytes for
500933 canonical bytes. Complete commit/restart/replay/check/drop0.507s;
stock takeover/retry/index/excision/post-write3.407s. Ten thousand disabled
publication calls246µs with zero captures. Counts/policy bytes are not RSS.

Continuation: no known required Goal8 gap remains. Goal9's selective backup reads,
log-cache reuse and effective maintenance controls, then Goal10 integration,
remain unfinished. No historical upgrade matrix or optional telemetry platform
is part of this handoff.
