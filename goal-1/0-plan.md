# Goal 1 — Make the Native Database Usable Without Example Harnesses

## Objective

Deliver a configurable local transactor executable, a safe administrative CLI,
and a reproducible application deployment over the existing Rust/PostgreSQL
database. Complete the small application API gaps: database statistics,
asynchronous index requests, and time-ordered UUID helpers. An application
should use normal executables and public library APIs without depending on an
acceptance example or reimplementing its process lifecycle.

This is Stage 1 of the parent strategy in `/home/jake/Developer/atomic/goal-0/0-plan.md`.
The scaffold is fresh; no stage below is implemented merely by creating it.

## Constraints and known context

- Preserve the working database. `goal-archive/G3/` records passed native
  transaction, peer, history, program, recovery and integrated PostgreSQL
  acceptance. Those records are evidence, not active instructions or a reason
  to skip checks of changed behavior. Earlier passes are in G1 and G2.
- The current project is one unpublished Rust crate with public service,
  connection, peer and operator APIs plus example executables. Independent
  writer/peer operation already works. Package and expose it; do not rewrite
  the baseline or ship a binary that merely launches an acceptance harness.
- Use `/home/jake/Developer/atomic/datomic_pro_docs` for intended semantics and
  `/home/jake/Developer/atomic/1.0.7705` for architectural evidence. Preserve
  immutable values, serialized transactions, exact retry, schema/history,
  peer-local reads and explicit failure outcomes. JVM/wire parity is not a gate.
- Runtime accounts use restricted roles and do not migrate on startup. Keep
  administrative authority separate. Expose configured PostgreSQL TLS and I/O
  policy; plaintext development use must be explicit, not an inherited example
  default. Reuse the existing configuration policy; do not invent flags for
  unimplemented later-stage caches or introduce a new configuration framework.
  Redact credentials and subject data from diagnostics.
- Local submission must have a usable configured endpoint or safe discovery,
  restrictive permissions, and clear restart behavior. Coordinate its contract
  with Goal 2 without requiring cross-host networking in this child. Do not add
  a web health endpoint merely to satisfy a deployment checklist.
- Existing typed operations and monitoring are starting points. Goal 2 owns
  secure cross-host submission, cross-process wakeups with durable-log replay,
  checkpointed change consumption and public exact snapshot references. Goal 3
  owns full I/O accounting, safe cache-resident read independence, concurrent
  cache access, an opt-in bounded SSD cache, timing/metrics callbacks and hints.
  Goal 4 owns program/query expansion, plain-datom mock sources and log-query
  integration; Goals 5 and 6 own fulltext and partitions; Goal 7 owns final
  integrated acceptance, reader scaling and mixed analytics/application loads.
  Coordinate contracts and record dependencies; implementing these later
  capabilities is not a prerequisite for closing this local-deployment child.
- Deployment/admin documentation must distinguish process-local `db()` capture
  from an actual cached native query: present storage/protection dependencies
  honestly, and do not equate cursor node-read counts with all PostgreSQL calls.
  A live transaction-report queue is not a restartable durable consumer;
  preserve its existing lossless semantics. Do not promise offline databases,
  exactly-once external effects or indefinitely retained snapshot references.
- Keep commands safe around existing data: explicit targets, retention and
  destructive controls; preserve migration checksums and durable meaning.
  Measure relevant costs honestly. Broad administrative operations need not
  become cheap or constant-time as a prerequisite for providing a usable CLI.

## Stages

### 1. Establish the local transactor as a supported executable

**Status:** Pending.

**Outcome:** An operator can configure, start, observe and normally stop a local
transactor for an existing database independently of application processes.

**Focus:** Real executable entry points and documented configuration; existing
fenced service/indexer lifecycle; restricted runtime credentials, PostgreSQL
transport policy, local endpoint ownership/discovery and understandable failure
reporting. Keep endpoint choices compatible with the later network stage.

**Completion signal:** The executable starts against actual PostgreSQL with a
restricted writer role, serves a separate application process, reports useful
status, shuts down normally and restarts with documented endpoint behavior.
Invalid configuration or authority fails clearly without hidden provisioning,
and acknowledged or unknown transaction outcomes retain their existing meaning.

### 2. Expose safe administrative workflows

**Status:** Pending.

**Outcome:** Operators can provision/create databases, back up, verify, restore,
inspect/report status, and advance garbage collection through a coherent CLI.

**Focus:** Thin integration of existing migration, backup and operator APIs;
explicit database/repository selection and administrative authority; retention,
preview/destructive controls; useful progress, errors and exit status. Distinguish
healthy pending maintenance from corruption and bounded GC progress from complete
reclamation. Preserve interruption/retry and existing immutable-data safeguards.

**Completion signal:** Real commands exercise a small PostgreSQL lifecycle from
provisioning through backup/verification and separate-target restore, inspection
and deliberately authorized GC. Failure and retry checks show safe target
selection, preserved data and truthful status. Commands require no source edits
or acceptance-harness setup to perform their documented function.

### 3. Complete the small application API gaps

**Status:** Pending.

**Outcome:** Applications obtain database statistics, request indexing without
blocking for completion, and generate documented time-ordered UUIDs through the
public library API.

**Focus:** Expose snapshot history-datom and useful per-attribute counts for
`db_stats` through existing indexes/cursors, not a deep integrity-inspection
shortcut; state scan costs for filtered/time views rather than promise O(1).
Provide an asynchronous `request_index` that captures a finite target and
composes with the existing `sync_index`; implement UUID helper semantics from
the docs/source with clear time-ordering limits. Do not confuse these additions
with Goal 3's broader telemetry work or make ordinary requests eagerly recover
the whole database.

**Completion signal:** Public API checks demonstrate accurate, clearly scoped
statistics; a nonblocking index request progresses to its captured target without
chasing a moving head; `sync_index` observes completion. UUID tests establish the
documented layout/order and clock-related limitations without claiming global
transaction ordering. PostgreSQL-backed paths actually execute.

### 4. Prove and document ordinary local application deployment

**Status:** Pending.

**Outcome:** A developer can build and run the service, administrative commands
and a separate Rust application using documented configuration and permissions.

**Focus:** Reproducible build/run instructions, normal lifecycle and recovery,
credential separation and the public API path. Exercise schema/transactions,
query/Pull, immutable old/history values, status/statistics, index requests and
reopen; reuse existing acceptance evidence where appropriate and check affected
boundaries in proportion to risk. Explain current storage dependence, the scope
of I/O counters and live transaction reports without claiming the later-stage
read independence or durable consumption already exists. Record packaging and
operating limits plainly.

**Completion signal:** A clean local deployment workflow runs the actual
executables against PostgreSQL and a separate application uses public APIs
through restart/reopen, with permissions matching the documented model and no
ordinary eager compatibility materialization. Deployment/admin instructions
accurately distinguish local value capture, native querying and durable change
consumption. Material results and remaining cross-stage dependencies are
reconciled into Goal 0. This child is not the parent's finish line.

## Continuation

Scaffold only; implementation has not started. First reconcile the current code
and parent plan, then begin Stage 1. Keep Goal 1 as the sole active child until
its observable completion signals are established. Return to Goal 0 afterward
and scaffold or resume Goal 2 according to the parent's reconciled state.
