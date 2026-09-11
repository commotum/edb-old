# Goal 7 — Reliable stock services and clients

## Objective and ownership

Stock executables support persistent active/standby operation, bounded automatic excision, recoverable routing, usable asynchronous clients and clear health/readiness.

This is Stage 6 of [Goal 0](../goal-0/0-plan.md), owning AO04, AO05, AO-C01, P04, AO07; AO-C03 disposition.
Status: complete (2026-09-10); native implementation and relevant public/application acceptance verified.
Goal6 final acceptance passed; preserve completed EDN and all prior child work.
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

Reuse fenced service/standby/excision mechanisms and existing native transports. Retain configured indexing/listener/TLS settings across takeover. Add Rust Future/stream-facing adapters without blocking executor threads; distinguish dropping a waiter from cancelling an already durable transaction. Define liveness versus write readiness. Evaluate PID/multi-database packaging by actual supervisor needs.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, API and current-version integrity risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Stock executables support persistent active/standby operation, bounded automatic excision, recoverable routing, usable asynchronous clients and clear health/readiness.
**Focus:** Reuse fenced service/standby/excision mechanisms and existing native transports. Retain configured indexing/listener/TLS settings across takeover. Add Rust Future/stream-facing adapters without blocking executor threads; distinguish dropping a waiter from cancelling an already durable transaction. Define liveness versus write readiness. Evaluate PID/multi-database packaging by actual supervisor needs.
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
**Completion signal:** Actual separate processes show standby takeover, exact retries through route change, read continuity, committed excision progress under runtime roles, shutdown and failure recovery. Single-thread executor checks prove nonblocking public adapters; health endpoints reflect actual availability without leaking credentials. Packaging differences receive explicit user-oriented dispositions.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: return to Goal0 and activate Goal8 diagnostics. Reopen this
child only for a later integration defect in its service/client ownership.

## Reconciled native contract

The HA/deployment docs require persistent contenders, takeover and independent
peer reads; recovered lifecycle code serves only after acquiring authority.
Preserve resolve-once storage identity, configured indexing, native deployments,
partition defaults and verified transport settings across takeover. Add nonblocking
standby status/try-active and interruptible polling without rewriting service queues.
Stock automatic-election mode may be additive to existing fail-fast startup.
Health means the process/supervisor is functioning; readiness additionally needs
active writer authority and its configured listener. No credentials or subject data
belong in probes. Foreground supervisor identity and one database per process are
native deployment defaults; assess documented composition instead of copying PID files.

Automatic excision belongs to indexing and processes committed requests, including
restart recovery, with one resumable job and bounded admission/steps. The excision
doc explicitly permits whole-database work and reduced write availability: do not
invent constant-cost requirements. Reuse existing authenticated generation rewrite,
pins/checkpoints/root-last completion; account admitted eager phases honestly and
check cooperative cancellation. Narrow lease/epoch-checked runtime authority to
committed-request jobs, never broad restore/GC/activation grants. A concrete source
gap exists in writer tree adoption after generation replacement; repair it while
preserving receipt-first assessment and intentional predates-excision tombstones.

An additive bounded Rust Future/Stream facade performs blocking driver/evaluator
work off executor threads and owns cleanup of abandoned jobs/results. Capture read
bases at admission, include queue time in deadlines, demand-drive stream chunks,
fuse terminal errors and distinguish dropped waiters from transaction cancellation.
Native values retain their existing synchronous method/last-drop contract unless
explicitly wrapped; do not silently claim otherwise. Completion callbacks are
once-only native closures, not ported Java Executor machinery.

Connection-bound remote routing captures stable identity and verified TLS policy,
refreshes stale endpoints, and preserves known commit versus report-open failure.
Route recovery is not permission to replay arbitrary transactions or regenerate
request keys. Ambiguous outcomes must remain explicit and exact-retry safe.

## Implementation and verified evidence

Additive `ServiceOptions`, interruptible `TransactionStandby` status/try-active,
stock `--mode auto`, bounded status-only `/health` and `/ready`, connection-owned
`RemoteWriter`, and bounded Rust Future/Stream adapters are implemented. Public
native values retain their synchronous last-drop contract; abandoned facade-owned
values are cleaned on workers. No JVM executor/reader runtime is introduced.

Automatic committed-request excision now uses the existing authenticated rewrite
through retained cooperative job state, source-byte admission and narrow SQL35
runtime wrappers. Its original witness failed because indexing left A15 pending;
that witness now passes. Runtime writers remain trusted semantic interpreters:
SQL authenticates live lease/epoch, active identity, excision build kind and frozen
source coordinates, not the opaque Rust predicate itself. Generic restore,
activation, abandonment and GC privileges remain denied. Admission is1MiB plus64x
source payload, not a claimed allocator/RSS bound; whole-database phases remain
explicit. Capacity failure pauses maintenance without hiding pending work.

Accepted coverage (zero skips/ignored within these executed selections):

| Check | Verified result |
| --- | --- |
| `service_standby` |5/5;4 actualPG, options/native registry/default placement, rename/reuse, cancellation and takeover;21.99s |
| `excision_runtime_authority`, `postgres_runtime_roles` |1/1 each actual restrictedPG;2.67s/3.37s |
| `automatic_excision`, `operations_excision`, private stop/resume |4/4 optimized19.76s;1/1 optimized2.15s;1/1 actualPG4.76s; capture/candidate/activation restart, admitted-capacity pause, exact tombstones and healthy subsequent writes |
| Async private/public |4/4 private,5/5 pure public,2/2 actualPG11.12s; nonblocking current-thread executor, bounded retention, callbacks, captured reads/streams, dropped waiter→exact retry, socket and restart |
| `remote_transport`, `remote_routing_edges` |4/4 actualPG15.62s +1/1 restrictedPG/TLS2.77s; cached route, unknown outcome not auto-replayed, explicit exact retry, async routed calls, identity reuse, known commit with failed report opening |
| `atomic` binary tests |7/7 incl3 socket health checks;0.85s; bounded overload, shutdown and redacted configuration |
| `product_cli` |2/2, actual separate application/restart/recovery under runtime roles;10.35s; includes new async query/old-value stream |
| `remote_product` |Both tests pass: real stock auto-contender crash/takeover+excision6.56s; isolated-network evolving application/replacement/reference workflow14.83s |

Commands use offline Cargo with reduced debug artifacts; set `ATOMIC_POSTGRES_URL`
for configured selections and allow local socket/network fixtures. Build `atomic`
and `application_workflow` before executable tests. No historical executable or
format matrix is an acceptance gate. Reuse these results unless an affected path
changes; no mandatory duplicate optimized build or unrelated full-suite rerun.

Useful measurements:10,000 standby status polls3.298ms/zero attributedSQL;
complete native takeover/commit/index/retry/shutdown1.603s; saturated health
shutdown257ms. Stock20s-poll contender cancelled32ms; crash/takeover plus retry,
indexing/excision and post-excision write3.325s in the6.444s measured workflow.
Excision8/32/128 transaction inputs took0.385/0.493/4.324s complete resume/check/
shutdown/drop, with1.18/1.50/2.79MB policy accounts (not RSS). Async application
reports its complete worker/query/stream/cleanup cost, including14.719ms in the
second restart run. These are representative samples, not throughput guarantees.

Early failures were corrected test setup (schema must precede EDN attribute use,
native synchronous oracle outside Tokio, real initial basis/publication) and a
sandbox-denied listener bind; the corresponding actual checks then passed.
All-target Clippy completed with existing warnings; new touched-code warnings
were fixed. `git diff --check` passed.

Docs: `docs/application.md`, `docs/async-client.md`, `docs/operations.md`. Native
foreground supervisor identity and one-database-per-process packaging satisfy
AO-C03 without PID-file emulation; multiple library connections/services compose
deliberately. Existing report-open errors can misleadingly classify denied reads
as corrupt manifests; Goal8 should address diagnostics, not reopen commit semantics.
