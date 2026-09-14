# Goal 12 — One Native Transactor and Sufficient Controlled Behavior

## Objective

Turn the working PostgreSQL kernel and service prototypes into one
database-bound Rust transactor path. Ordinary callers submit declarative
transactions without racing on a mandatory basis; the active fenced writer
chooses the actual db-before and transaction time, expands controlled behavior,
validates the complete successor through `Database::with`, publishes exactly
once, and reports acknowledged, rejected, or genuinely unknown outcomes
without a competing write API.

## Constraints

- Follow `goal-9/EVIDENCE_LEDGER.md` C12–C16. `datomic_pro_docs` governs
  transaction, time, acknowledgement, and function semantics; recovered
  `transaction/create-procargs`, `update/process-transaction`, log CAS,
  lifecycle ownership, function execution, and base-plus-tail startup in
  `1.0.7705` are the default control-flow blueprint.
- Rust throughout, PostgreSQL only. Bind fencing and serialization to the
  concrete database being served; do not create backend or consensus
  abstractions.
- Keep `Database::with` pure and clock-explicit. Server-selected time,
  serialization, retry identity, and publication belong outside the kernel.
- Expected-basis compare-and-set may remain an explicit conditional extension,
  but must not be required for ordinary transactions.
- There is one authoritative application write path. Raw storage CAS may
  remain crate-internal for the transactor/indexer/operations implementation;
  peers and callers may not bypass fencing and assessment.
- A durable commit cannot be returned as an ordinary failure. Timeouts and
  connection loss after the decision point are unknown outcomes resolved by a
  durable request identity.
- Persisted behavior must be deterministic, database-local, versioned,
  resource-metered, and without ambient filesystem/network/clock/randomness.
  Add only the expressive forms needed for useful db-before transformations
  and complete db-after invariants; do not build a general language runtime.
- Startup and failover use a verified durable base plus authoritative log tail
  when available. Goal 13 owns the scalable persistent-tree representation;
  this goal owns transactor adoption of a verified checkpoint rather than
  unconditional genesis replay.

## Known context

- Goals 10 and 11 establish collision-free identity, exact valid successors,
  schema/idents as ordinary information, canonical **ATMC v3** durable values,
  and exact PostgreSQL recovery. ATMC v3 datom-bearing payloads keep their
  frozen stored-value-before-T ordering; Goal 16's source-corrected
  logical-E/A/V then descending-T/op order
  is a separate **ATIX tree v4** format, not a reinterpretation of v3 bytes.
- `PostgresStore::transact` and `Peer::transact` currently expose mandatory
  expected basis and caller-supplied transaction time. `PostgresStore` is also
  a public unfenced writer beside `TransactionService`.
- Lease scopes are caller strings rather than a proven database binding, and
  peer/store/service publication paths need one ownership audit.
- The service already has bounded admission, durable idempotency, unknown
  outcome handling, epochs, and useful fault tests. Preserve those working
  pieces rather than replacing them wholesale.
- The persisted instruction runtime is deterministic and bounded but its
  current straight-line arithmetic/data surface is not sufficient for the
  documented db-before transformation and db-after predicate roles.
- Peer index manifests can now be independently rejected and log recovery is
  exact, but transactor/service startup still needs an explicit verified
  base-plus-tail state path.

## Stages

### 1. Source contract and write-path audit

**Status:** Complete (2026-09-03).

**Outcome:** One executable contract identifies the intended submission,
serialization, time, ownership, decision, report, function, and recovery
boundaries, and maps every current public mutation route to keep, internalize,
or remove.

**Focus:** Trace docs and 1.0.7705 control flow; distinguish semantic behavior
from PostgreSQL fencing mechanics; add red witnesses for ordinary concurrent
submission without stale-basis failures, database-bound fencing, server time,
and acknowledged/unknown resolution.

**Completion signal:** The contract cites exact source/doc anchors, the public
write-path inventory has no ambiguous owner, and focused witnesses fail for
the observed reasons rather than scaffold omissions.

**Evidence:** `CONTRACT.md` fixes the normative submission, serialization,
time, decision, report, controlled-behavior, ownership, and recovery boundaries
against exact documentation and recovered-source anchors. It inventories every
logical and exceptional mutation route with one disposition, and separates
durable request keys, PostgreSQL lease epochs, bounded admission, and the
deterministic sandbox from Datomic's promises. The audit recorded concrete
baseline gaps—mandatory client basis/clock, independent request/lease database
scope, public bypasses, post-commit report replay, insufficient program control
flow, and genesis-only cold recovery—and focused tests now encode the Stage 2/3
repairs. This status claims the audit is complete, not that those repairs pass.

### 2. Database-bound serialized submission

**Status:** Complete (2026-09-03).

**Outcome:** A single active transactor serializes ordinary submissions against
its actual current db-before, with an optional explicit compare-basis form and
server-assigned valid transaction time.

**Focus:** Database-derived lease/fence identity; authoritative queue/state;
internal head CAS; safe direct-store boundary; peer/client submission; time
override bounds; declarative retry digest independent of transient basis/time.

**Completion signal:** Independent concurrent nonconditional requests both
commit in a serial order without client retry; conditional requests still
detect mismatch; a lease for one database cannot fence or publish another;
and invalid past/future time overrides abort before publication.

**Evidence:** `TransactionServiceConfig` now binds one database and
`TransactionRequest` carries declarative operations plus optional
compare-basis/import-time constraints, but no database id, required basis, or
required clock. PostgreSQL locks the real head, derives request identity without
transient selected basis/time, samples `clock_timestamp()`, applies the
recovered inclusive basis/future bounds, and carries the exact assessed
`db-before` into the receipt. Leases are database identities, reject duplicate
live acquisition even under the same holder label, and fail closed when paired
with another database. `Peer` no longer owns an unfenced writer. Real
PostgreSQL witnesses in `service_worker`, `service_leadership`,
`service_stress`, and `tx_instant_semantics` establish two ordinary concurrent
successes, explicit conditional conflict, database fencing, lossless ordering,
server time, and invisible invalid overrides. The focused gate passed 21 tests
including peer/base integration on 2026-09-03.

### 3. Decision, acknowledgement, and report integrity

**Status:** Complete (2026-09-03).

**Outcome:** Every published request has one durable commit decision; an
admitted attempt otherwise returns a known precommit rejection or a genuinely
unknown outcome, with one coherent report path across overload, timeout,
connection loss, process death, and leader replacement. Rejections are not
made permanently sticky across later database values.

**Focus:** Decision point; request lookup; no post-commit reconstruction as the
normal success path; report ordering/subscription; shutdown; unknown-outcome
reconciliation; removal of unfenced public publication routes.

**Completion signal:** Fault injection proves no ordinary error follows a
known commit, rejected/precommit work is invisible, an ambiguous retry cannot
double-commit, reports match the originally assessed value, and failover never
forks or publishes under a stale/database-wrong fence.

**Evidence:** The public application surface now has exactly one logical
database publisher: `TransactionClient` -> the database-bound service worker
-> the crate-private fenced PostgreSQL transition. Raw store, lease, fault, and
direct program-transaction paths were internalized or removed; publication
kill points live only in crate tests. The older mutable named-program catalog
is non-authoritative operator metadata and cannot select transaction or
predicate behavior; its remaining public cleanup belongs to Goal 15 and is not
counted as Datomic function activation. Real PostgreSQL tests establish
invisible failures at each precommit boundary, a durable unknown outcome that
resolves to its exact original receipt, idempotent replay without a second
transaction/report, originating response before subscription broadcast,
lossless commit-order reports, stale/wrong-database fencing, standby takeover,
and process death before commit. The focused non-skipping gate passed the
internal fault tests, 8 service-worker tests, 3 leadership tests, and the
standby failover test on 2026-09-03.

### 4. Sufficient deterministic functions and predicates

**Status:** Complete (2026-09-03).

**Outcome:** Persisted transaction functions can make bounded decisions from
db-before and emit ordinary transaction data, while persisted entity/attribute
predicates validate the complete proposed db-after through the same fenced
path.

**Focus:** Minimal conditional/branching, bounded iteration, local data/query
access, structured emitted forms, typed results, fuel/stack/output limits,
immutable blob deployment plus temporal `:db/fn` binding, recursion and panic
containment.

**Completion signal:** Source-witness programs implement nontrivial conditional
db-before transforms and cross-field db-after invariants, remain byte/result
deterministic across restart/failover, exhaust every resource bound safely,
and cannot bypass ordinary transaction validation.

**Evidence:** Canonical program ABI v4 provides typed runtime maps/vectors,
structured `If` and bounded `ForEach`, full entity-map and list-form output,
nested calls, exact-value predicate results, Datomic-shaped cancellation, and a
bounded conjunctive Datalog host. One configured `ProgramBudget` spans sibling
and nested functions, predicates, queries, allocation, forms, and encoded
bytes. Immutable content-addressed blobs are selected only by temporal
`:db/fn` information in the locked db-before; predicate symbols resolve lazily
only for surviving assertions or explicit ensures, and new schema/function
bindings take effect on the following transaction. A shared byte/count LRU
retains validated decoded programs without changing their logical identity.
Real PostgreSQL witnesses in `program_transactions`, `program_cache`, and
`program_recovery_failover` prove branch/map inspection, finite transformation,
a genuine two-pattern join, nested and sibling identical-db-before reads,
cross-field db-after predicates, exact non-true diagnostics, cancellation and
resource failures with no publication, temporal rebinding, base recovery, and
standby takeover. The focused gate passed 6, 2, and 3 tests respectively.
`SemanticError::anomaly` retains the exact bounded typed cancellation map,
including nested values and the added `:datomic/cancelled true` fact; the older
string detail map remains only a convenient diagnostic projection.

The runtime intentionally uses numeric attribute ids in its compact canonical
IR. That is a native ergonomics/portability restriction relative to keyword
lookups in Clojure functions, not a claim that Datomic imposes it; full query
surface and ident-oriented planning remain Goal 14 work.

### 5. Verified base-plus-tail recovery and integrated closure

**Status:** Complete (2026-09-03), with scalable base representation explicitly
deferred to Goal 13.

**Outcome:** Startup/failover adopts the newest independently verified base it
can trust, replays only the authoritative tail, and resumes the single writer
without changing observable database or report semantics.

**Focus:** Checkpoint selection/binding; tail hash continuity; corrupt-base
fallback; temporal function information; takeover catch-up; recovery work
witness; parent-plan fold-back.

**Completion signal:** Real PostgreSQL restart and leader-failover tests prove
base-plus-tail equals genesis replay, corrupt/interrupted bases fail closed,
only the authoritative log tail after the selected base is decoded, formatting
and warning-denying Clippy pass, and Goal 9 Stage 3 can be marked complete.

**Evidence:** Append-only index publications bind database, basis, transaction
hash, and manifest hash. Versioned semantic state commitments bind every
accepted base to the authoritative current information and allocation
frontier, while the separately bound transaction hash authenticates complete
chronology, so a self-consistent forged manifest cannot substitute different
current facts. Startup selects the newest verified base, rejects corrupt or
unpublished candidates, and replays the contiguous authoritative tail.
`:db/noHistory` remains an indexing-job storage policy, not a semantic erasure
contract. Ordinary root-plus-tail recovery preserves its selected base and
does not resurrect omissions while incrementally indexing it. An explicit
administrative rebuild from the authoritative log may produce a different
admissible retained-history subset: the docs say the flag controls future
indexing jobs, not current values or precise removal
(`03_schema/00_schema_data_reference.md:201-213`,
`03_schema/01_changing_schema.md:62-73`). Real PostgreSQL tests prove
coherent-forgery rejection,
corrupt-base fallback, exact base-plus-tail equality, standby recovery, and two
actual server restarts. The strengthened server-restart witness compares every
current/history index of the recovered db-before with the exact pre-restart
committed database rather than trusting a counter.

This stage does **not** claim recovery is O(tail) overall. The current base is
still eagerly decoded/materialized; although new state commitments are O(1)
from incrementally carried roots, legacy-v1 verification can still scan its
projection. Goal 13 owns shallow persistent roots, lazy
segment reads, bounded residency, and incremental commitments. Here the proved
boundary is narrower and source-faithful: a verified durable base is adopted
and only post-base authoritative transaction records are replayed.

## Exit condition

Goal 12 completes only when every ordinary write flows through one
database-bound fenced Rust transactor, normal submissions do not require a
client basis or clock, controlled behavior is sufficient for documented
db-before/db-after roles, acknowledgement semantics survive injected failure,
and verified base-plus-tail recovery resumes the exact committed state.
