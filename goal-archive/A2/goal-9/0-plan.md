# Goal 9 — Source-Backed Corrective Reconstruction

## Objective

Reopen and complete Goal 0 by repairing the native Rust Datomic-inspired
database wherever the current implementation contradicts `datomic_pro_docs`,
the sound architecture and algorithms recovered from `1.0.7705`, or its own
production guarantees. Preserve the working semantic and PostgreSQL kernel,
but replace prototype shortcuts rather than documenting them into acceptance.

This is a corrective parent goal. It creates one child scaffold for each major
repair stage, executes that child through observable behavior, folds the
evidence back here and into `goal-0/`, and continues until the original Goal 0
success condition is genuinely established.

## Constraints

- `datomic_pro_docs` is the semantic authority. `1.0.7705` is the default
  implementation and performance blueprint and must be traced at the relevant
  namespace, type, representation, algorithm, cache, and control-flow boundary.
- Rust remains the implementation language and PostgreSQL the only store.
  Do not add backend portability, a JVM/Clojure runtime, or legacy wire/byte
  compatibility.
- Preserve the closest sound recovered design. Deviate only for documented
  semantics, Rust safety/idiom, or a concrete PostgreSQL advantage; record the
  evidence and consequence rather than labeling architectural behavior “JVM
  machinery.”
- Keep `Database::with` a pure, declarative, unordered transition over complete
  database values. Preserve immutable datoms, history, identity, time views,
  and peer-observable behavior.
- Keep one authoritative, serialized, database-bound write path. Storage CAS
  is an internal publication mechanism, not mandatory application transaction
  semantics.
- Schema, idents, and persisted database behavior are information. Typed
  structs and caches should be derived views, not competing authorities.
- Persistent indexes must support bounded, lazy peer operation and incremental
  consolidation; a full in-memory representation may remain only as an oracle.
- PostgreSQL integration evidence must prove it actually ran. Missing
  prerequisites may skip explicitly or fail the acceptance harness, but may
  not count as a passing integration test.
- Preserve unrelated work in the dirty working tree. Do not claim a child or
  this parent complete because its scaffold exists or because tests exercised
  only self-skipping paths.

## Known context

- `goal-1/` through `goal-8/` contain useful contracts and implementation, but
  their completion labels are historical rather than current acceptance.
- `EVIDENCE_LEDGER.md` is the verified repair baseline. It also records prior
  audit claims that were narrowed or refuted during the source cross-check.
- Goals 10 through 15 now close identity/successor validity, schema authority,
  fragmented writes and acknowledgement, persistent/lazy peer internals,
  exact query-source propagation, and PostgreSQL lifecycle safety. Goal 16 is
  the first unfinished child.
- Rechecking Goal 13 against the recovered tiered `Db` exposed a distinct
  production-writer gap: `PostgresStore.current` still retains the complete
  eager oracle even if peers and background publication become lazy. Goal 16
  owns removing that shortcut after Goal 14 supplies the common semantic
  access seam.
- Goal 16 implementation exposed additional public-boundary gaps that the old
  “integrated Goal 17” bucket would have hidden: ordinary `Peer::db`/`sync`
  still materialize the eager oracle, peer reports discard database values and
  tempids, native reverse/temporal index cursors are absent, transaction tuple
  refs are not representable, and default query controls impose undocumented
  semantic ceilings. Source-backed ledger entries C32–C38 now split those
  repairs into Goals 17–19; Goal 20 is the integrated gate.
- The current PostgreSQL log/head/idempotency transaction is a strong base and
  should be retained while its APIs and surrounding service are corrected.

## Stages

### 1. Kernel identity and valid-state repair (`goal-10/`)

**Status:** Complete 2026-09-03. See `goal-10/0-plan.md` and
`goal-10/IDENTITY.md`; pure, real-PostgreSQL, and explicit restart gates pass.

**Outcome:** Every database value uses collision-free transaction, system, and
user identity; allocation is frontier-safe and checked; every successful
transaction is valid under its successor schema and is exactly recoverable.

**Focus:** Recovered `make-eid`/`t->tx` structure; issued-ID frontier; explicit
ID rules; same-transaction schema alteration validation; tuple installation
invariants; structural deterministic normalization; persistent numeric tie
handling; focused corruption and transaction-1000 witnesses.

**Completion signal:** Regression fixtures demonstrate no identity alias at or
beyond transaction 1000, no explicit-ID/tempid collision, invalid schema/data
combinations abort before publication, every accepted transition round-trips
through durable recovery, and all pure gates pass.

### 2. Schema and idents as information (`goal-11/`)

**Status:** Complete 2026-09-03. Exact native genesis, positive schema
transactions, material install/alter hooks, general idents and aliases,
current-basis temporal interpretation, format-v3 log/base/backup recovery, and
coherent-base rejection are implemented. Format, warning-denying Clippy, all
pure tests, and a fresh non-skipping PostgreSQL 15.11 full suite (149 passed,
one subprocess worker intentionally ignored) pass, including two actual server
restarts. See `goal-11/0-plan.md`.

**Outcome:** Schema, general idents, aliases, enumerations, and persisted
database behavior are represented as ordinary immutable datoms, with typed
schema/ident indexes derived synchronously from the database value.

**Focus:** Bootstrap datoms; system identities; install/alter hooks;
general keyword-to-entity resolution in E/A/V positions; alias behavior;
queryable schema history; canonical encoding/recovery and migration from the
current side-channel representation where a deliberate compatibility boundary
is needed.

**Completion signal:** Ordinary queries can observe schema/ident information,
enum idents work as references, rename aliases resolve correctly, typed caches
rebuild solely from authoritative datoms, and schema evolution/recovery witness
the documented current-schema time-view rules.

### 3. One transactor and sufficient controlled behavior (`goal-12/`)

**Status:** Complete 2026-09-03. See `goal-12/0-plan.md` and
`goal-12/{CONTRACT,RUNTIME_BLUEPRINT}.md`.

**Outcome:** All writes flow through one database-bound, fenced, serialized
service that evaluates declarative transactions against the current db-before,
assigns valid transaction time, integrates persisted functions/predicates, and
reports acknowledged/unknown outcomes correctly.

**Focus:** Optional rather than mandatory compare-basis; server transaction
time; db-bound leases; removal of unfenced peer/store bypasses; one report path
without post-commit recovery; base-plus-tail transactor recovery; db-before
function expansion and complete db-after entity validation; deterministic,
metered persisted runtime with sufficient branching/iteration/data access.

**Completion signal:** Concurrent independent transactions serialize without
spurious stale-basis failures, every publication is fenced to its database,
functions and predicates work through that same HA path, acknowledged commits
cannot be reported as ordinary failures, and failover/retry/restart tests pass.

**Evidence:** Ordinary submissions now carry one unordered `Vec<TxForm>` with
no mandatory basis or clock; one database-bound service selects db-before and
time, expands temporal functions, validates the complete successor, and
publishes through a crate-private fenced transition. Durable retry decisions,
unknown outcomes, reports, and standby takeover share that path. ABI-v4
persisted behavior supplies structured control/data, nested calls, a bounded
conjunctive-query host, lazy assessed predicate resolution, shared quotas,
typed cancellation data, and immutable cached program content. Append-only
root publications plus authoritative semantic commitments reject coherent
forgeries and permit verified-base plus exact-tail startup. Non-skipping real
PostgreSQL gates include process death, concurrent load, standby failover,
program/predicate recovery, corrupt-base fallback, and actual server restart
with complete recovered-index comparison.

The recovery claim is intentionally not O(tail) overall: current bases are
still eagerly materialized and state commitments are full-state work. Goal 13
owns that architectural/performance correction. The mutable named-program
catalog left by the earlier prototype is non-authoritative operator metadata;
Goal 15 owns its removal from the public/backup lifecycle surface.

### 4. Persistent index and live peer architecture (`goal-13/`)

**Status:** Complete 2026-09-03. See `goal-13/0-plan.md` and
`goal-13/ARCHITECTURE.md`. Canonical shallow native trees, source-shaped sparse
separators, a persistent fanout-16 recent tier, affected-range publication,
independent physical revisions, lazy immutable cursors, bounded shared caches,
atomic cloneable peer advancement, opt-in reports, and same-basis root repair
pass focused pure and real-PostgreSQL gates. A localized three-datom successor
read 83 nodes against an 854-node base, wrote 35 candidate nodes, and reused
832 subtree references without weakening the tenfold witness.

**Outcome:** Immutable shallow index trees plus a bounded recent layer support
incremental publication, lazy local reads, shared immutable database values,
and long-running peers without materializing all database history.

**Focus:** Root/directory/leaf representations; affected-range merges;
base-plus-tail adoption; cache-backed seeks; current/history tiering;
thread-safe shared connections; atomic tail/hash/generation adoption;
snapshots/pins and superseded-root retirement.

**Completion signal:** Measured consolidation work is sublinear for localized
updates, cache/resident memory remains bounded as history grows, peers perform
lazy segment reads, old snapshots stay valid, corrupt or interrupted roots fail
closed, and restart/catch-up results equal the reference model.

### 5. Query, pull, and entity conformance (`goal-14/`)

**Status:** Complete 2026-09-03. See `goal-14/0-plan.md`. One immutable
eager/native database value now governs patterns, lookup refs, helpers,
extensions, rules, pull, and entities. Source-witness fixtures repaired the
claimed primitive/pull defects and an independent closure audit exposed and
closed five additional gaps. The focused semantic gates, all-target Clippy,
full pure suite, and an 18-test live-PostgreSQL peer gate (including restart
and generated eager/native/scan differential) pass.

**Outcome:** Every claimed query/pull operation evaluates against its exact
database value and matches documented/recovered semantics without artificial
limits masquerading as language semantics.

**Focus:** Temporal/filtered source propagation through expressions,
extensions, aggregates and pull; general entity identifiers and aliases;
`get-some`, `ground`, tuple nil, min/max bag behavior; adorned rule evaluation
with invocation bindings and resource controls separated from semantic
termination; independent 1.0.7705 witness fixtures.

**Completion signal:** Source-witness fixtures cover each corrected behavior,
historical queries cannot observe future data, rule results converge without a
fixed semantic round ceiling, and indexed execution agrees with an independent
reference evaluator over generated cases.

### 6. PostgreSQL lifecycle and operational safety (`goal-15/`)

**Status:** Complete 2026-09-03. See `goal-15/0-plan.md` and
`goal-15/OPERATIONS.md`. An independent source audit found and closed raw
history-as-of component traversal, datom-local keeper, `noHistory`, migration
checksum, GC dependency order, active-generation inspection, and runtime ACL
gaps before acceptance.

**Outcome:** Migration, security, backup/restore, integrity, GC, and excision
can be operated with truthful durability, privilege, bounded-growth, and
privacy guarantees.

**Focus:** Migration/runtime role separation and forward-version checks; TLS
configuration; coherent inspection snapshots; differential root-last backup
identity and crash-safe file publication; exact restore verification;
retirement of superseded derived roots; source-faithful excision keeper,
component/reference closure, audit, synchronization, and explicit limits;
database-scoped fault isolation when a different database has a corrupt derived
manifest.

**Completion signal:** Least-privilege service/peer roles can run without DDL,
remote connections can require TLS, crash/retry backup and exact restore pass,
normal consolidation storage is reclaimable after a safe boundary, inspection
is race-free, excision tests prove precisely scoped removal/invalidation, and a
corrupt derived root for database A cannot abort logical maintenance for
database B or cause shared garbage collection to delete possibly referenced
content.

**Established evidence:** The final PostgreSQL-enabled library gate passes
135/135 nonignored tests (one explicit subprocess helper ignored). Fresh
non-skipping suites pass backup/restore 9/9, GC 13/13, inspection scope 2/2,
integrity 2/2, excision 1/1, migration/runtime boundary 2/2, and exact runtime
ACL/search-path 1/1. Historical migrations remain byte-authenticated; runtime
DDL is separated; portable roots publish last; restore/excision activate exact
database-local generations; GC follows durable provenance and pins/grace; and
the operator contract states native-format, encryption, WAL/replica/backup,
disconnected-reader, request-tombstone, and administrative-authority limits.

### 7. Tiered production transactor (`goal-16/`)

**Status:** Complete 2026-09-04. See `goal-16/0-plan.md` and
`goal-16/ARCHITECTURE.md`. The fenced writer now assesses against an immutable
native root-plus-tail value, updates a persistent authenticated commitment by
path copy, publishes through the existing atomic PostgreSQL decision, and
retains bounded recent/cache/root/metadata state rather than a full eager
database.

**Outcome:** The authoritative Rust writer uses the same immutable durable
roots and bounded memory/indexing tiers as the peer-facing database value;
the eager full-state `Database` remains a pure oracle, not production state.

**Focus:** A lazy semantic `IndexAccess` boundary shared with Goal 14;
transaction expansion and validation over exact ranges; persisted functions
and predicates against the actual db-before/db-after; incremental commitment;
bounded memory/indexing handoff; reports without retaining historical
databases; activation and failover from verified root plus tail.

**Completion signal:** Long-history real-PostgreSQL witnesses show production
writer residency follows configured recent/cache bounds rather than total
database history; localized transactions load bounded index ranges, preserve
all Goal 12 acknowledgement/fencing semantics, and produce exactly the eager
oracle result across restart and failover.

**Established evidence:** A 128-commit residency test, fixed-seed and
adversarial eager/native differentials, background handoff, backlog,
contention, failover, corruption, exact retry, and service recovery pass on
real PostgreSQL. Fresh broad runs pass peer 18/18 and GC 15/15; separate peer
and transactor tests each perform a real PostgreSQL 15.11 stop/start and reopen
the exact committed native value without compatibility materialization.
Migration 22 closes the retirement/build-intent dependency found only when the
full GC suite ran serially and successfully heals the actual stranded v21
fixture. The remaining eager public peer methods are not writer fallbacks and
remain explicitly owned by Goal 17.

### 8. Native connection and immutable database API (`goal-17/`)

**Status:** Pending.

**Outcome:** The ordinary application boundary is one cloneable connection
whose `db`, synchronization, transaction, report, entity, query, pull, and raw
index operations all use exact native immutable values; the eager database is
an explicitly named oracle only.

**Focus:** C32–C35; combine the advancing peer and transaction client; make
native values the default `db`/`sync` result; complete peer reports; expose
lazy forward/reverse cursors over current/history/time views; t/tx/instant time
points and `entid-at`; preserve explicit eager-oracle/admin escape hatches.

**Completion signal:** A source-witness API suite proves no ordinary peer call
materializes the eager oracle; one connection transacts and observes complete
reports in source order; raw index iteration is lazy in both directions across
durable/recent and temporal/history values; all time-point forms agree.

### 9. Transaction input and speculative-value closure (`goal-18/`)

**Status:** Pending.

**Outcome:** Every documented entity-reference shape can cross the native
transaction boundary, and speculative native database successors remain real,
chainable immutable values rather than one-shot validation overlays.

**Focus:** C36 and the remaining part of C38; recursive transaction-only tuple
elements; ref-valued lookup keys; db-before identifier resolution; V-only
tempid rejection; unique/upsert ordering; versioned request/program encoding;
public pure `DatabaseValue::with`; chainable bounded overlays; transaction-local
read memoization and truthful I/O accounting.

**Completion signal:** Eager/native/generated differentials cover scalar and
tuple refs by id, ident, lookup ref, and tempid, including invalid V-only use;
multiple speculative successors can be chained without full materialization;
durable/retry/restart encoding remains exact and old supported payloads decode.

### 10. Query/pull limits and surface truth (`goal-19/`)

**Status:** Pending.

**Outcome:** The implemented local query and pull model has no accidental
finite semantics, and every documented but unimplemented API is either supplied
idiomatically in Rust or classified as a genuine non-core omission against the
original objective with no misleading parity claim.

**Focus:** C37; remove default result/work/entity/depth ceilings; stack-safe
unlimited pull recursion; audit fulltext, nested `q`, log query helpers, random
aggregates, return maps, pull transforms, `qseq`, index-pull, and transaction
hints against docs and recovered source; implement the central/low-machinery
pieces and record why JVM/classpath or specialized secondary features remain
out of the core port.

**Completion signal:** Large-result and deep-recursion witnesses succeed by
default and fail only under explicit controls; the supported surface has direct
semantic fixtures; the deviation ledger names every remaining documented API
gap and demonstrates that none weakens Goal 0's central benefits.

### 11. Integrated production evidence (`goal-20/`)

**Status:** Pending.

**Outcome:** The corrected pieces operate as one PostgreSQL-backed Rust system,
and Goal 0 can be closed without exceptions hidden as “native boundaries.”

**Focus:** Explicit integration harness; same-database contention; long
histories and data larger than cache; abrupt process/server failure; recovery
latency; mixed-version rejection; observability; public connection workflow;
class/namespace-to-Rust and deviation ledger reconciliation.

**Completion signal:** A reproducible gate provisions PostgreSQL, proves every
integration/restart/fault test actually ran, meets stated scale/boundedness
criteria, and maps every original Goal 0 guarantee to coherent working behavior
or a documented non-core omission that does not weaken the objective.

## Success condition

Goal 9 is complete only when all corrective children are complete, the
integrated gate establishes the original `goal-0/` success condition, and Goal
0 is updated from reopened to achieved with no known correctness,
architecture, or evidence gap hidden by scope reduction.
