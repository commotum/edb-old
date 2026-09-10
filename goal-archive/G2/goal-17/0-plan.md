# Goal 17 — Native Connection and Immutable Database API

## Objective

Make one cloneable Rust connection the ordinary application boundary for the
native database. Its `db`, synchronization, transaction, report, entity,
query, pull, and raw-index operations must all preserve exact immutable native
database values without eager oracle materialization. Complete the recovered
forward/reverse index, time-point, report, and background synchronization
contracts while retaining Goal 16's bounded root-plus-tail architecture.

## Constraints

- `datomic_pro_docs` is the semantic authority. Recovered Datomic Pro 1.0.7705
  connection state, `Db`, notification, index traversal, temporal, and sync
  paths are the default implementation blueprint.
- Rust throughout; PostgreSQL is the only store. Reuse the concrete fenced
  transaction service, native trees, recent BTSet, immutable cache, pins, and
  publication coordinates. Do not introduce storage portability.
- Ordinary APIs may not silently call `recover_to`, populate an eager
  `Database`, or fall back from a missing native root. Eager behavior remains
  explicitly named oracle/administrative functionality.
- Preserve one shared monotonically advancing connection and immutable old
  database values. Synchronization changes what later `db` calls see; it never
  mutates an already captured value.
- Reports are opt-in, source ordered, and complete: db-before, db-after,
  tx-data, and tempids. A disabled/removed queue retains nothing; queued values
  own visible root/generation pressure.
- Raw index access is lazy in both directions across durable and recent tiers.
  Temporal/filter/history behavior and deferred corruption errors must not be
  hidden behind collection conveniences.
- Accept documented time points explicitly: basis `t`, transaction entity,
  and instant. `as-of` is inclusive, `since` exclusive, T/Tx agree exactly,
  and instant selection retains its documented imprecision.
- Include `sync-index`, `sync-schema`, and `sync-excise`; same-basis physical
  publication revisions are real progress. Do not confuse logical schema
  presence with physical AVET readiness.
- Keep Goal 18 transaction-value representation and speculative `with`, Goal
  19 query/pull surface expansion, and Goal 20 deployment harness out of this
  goal except where a narrow API seam is required.
- PostgreSQL tests count only when they actually execute. Preserve all prior
  migration bytes and Goal 16 writer/failure guarantees.

## Known context

- Goal 16 completed the tiered writer and supplies exact native
  `DatabaseValue`, `TieredSnapshot`, complete native service reports, bounded
  caches/recent roots, and one fenced `TransactionClient`.
- `Peer` already shares live state through `Arc`, but ordinary `db`, `try_db`,
  `sync`, and `sync_to` return eager `Arc<Database>`; native methods have
  secondary names and connect may silently rebuild/materialize if no root is
  present.
- The peer report queue stores durable transaction rows rather than complete
  immutable before/after values and tempids. The service already has the
  correct native report shape and submitter-before-observer sequencing.
- The durable and recent cursor machinery is forward-only at the public seam;
  reverse access would currently require collection/reversal. Temporal/custom
  windows also retain collecting paths that must be made honestly streaming.
- `DatabaseValue::as_of`/`since` accept only decoded `u64` T values. Instant and
  transaction-entity resolution exists only on the eager oracle.
- Goal 9 evidence C32--C35 is the corrective baseline. C38 is complete for
  transaction-local exact-read memoization; optional client hint/prefetch is a
  separate performance omission, not a Goal 17 correctness requirement.

## Source blueprint

- Connection/db/sync: docs
  `04_transactions/06_client_synchronization.md:12-35`,
  `07_peer_api/01_java/02_connection.md:31-181`; recovered
  `peer/src-clj/datomic/peer.clj:529-549,633-715,770-793,1579-1631`.
- Immutable tiered value and lazy merge: recovered
  `peer/src-clj/datomic/db.clj:4719-4744,5035-5074`.
- Reports and ordering: docs
  `04_transactions/03_processing_transactions.md:25-59`; recovered
  `peer.clj:633-715,727-741`.
- Raw forward/reverse traversal: docs `06_indexes/04_index_apis.md` and
  `06_indexes/05_rseek_datoms.md:8-48`; recovered
  `db.clj:1810-2230` and `btset.clj:157-416`.
- Time points and `entid-at`: docs
  `02_core_concepts/02_database_filters.md:39-81`; recovered
  `db.clj:3995-4095,5139-5147`.
- Index/schema/excision synchronization: Java connection reference
  `07_peer_api/01_java/02_connection.md:49-63,116-145`; recovered
  `peer.clj:332-421,770-793,1223-1236`.

## Stages

### 1. Exact public contract, time points, and raw bounds

**Status:** In progress 2026-09-03. Explicit `TimePoint::{T, Tx, Instant}`
now follows recovered `as-of-t`/`entid-at`, including earliest exact
duplicate-millisecond selection, inclusive as-of, exclusive since, and the
separate first-at-or-after instant rule for entity boundaries. Typed
zero-through-four-component raw boundaries now fixture forward-lower and
reverse-upper positioning in every index, checked T/Tx normalization, AVET
qualification, and VAET reference requirements. Focused pure witnesses pass;
the remaining Stage 1 work is exact-value ident/lookup-ref/schema
normalization at the public raw boundary plus a non-skipping native
PostgreSQL bounded-read witness.

**Outcome:** The public types can express every documented native time point
and zero-to-four-component raw index boundary without sentinels or eager-only
interpretation.

**Focus:** Reconcile the present public API with C32--C35; define explicit
`TimePoint`; resolve T/Tx/instant and `entid-at`; represent E/A/V/T raw seek
components with schema/ident/ref normalization and AVET readiness; fixture the
source ambiguity for duplicate transaction instants.

**Completion signal:** Pure eager/native source witnesses agree for T and Tx,
prove documented instant edge cases, and distinguish every forward/reverse raw
boundary in all four indexes before the facade is changed.

### 2. Lazy bidirectional native cursors and time views

**Status:** Pending.

**Outcome:** Native database values expose fallible streaming forward and
reverse index traversal across durable trees, recent BTSet, current/history,
time windows, and filters.

**Focus:** Right descent, upper-bound and predecessor traversal in immutable
tree/recent structures; reverse tier merge and current collapse; virtual
boundary semantics; lazy window/filter application; bounded reads, cache
sharing, old-cursor pins, and deferred corruption.

**Completion signal:** Multi-directory/multi-leaf real-PostgreSQL fixtures
match the eager oracle in both directions for every index/view; constructing a
cursor reads no child and taking one item reads a bounded path without
collecting or retaining database-sized data.

### 3. One strict-native cloneable connection

**Status:** Pending.

**Outcome:** One canonical `Connection` combines transaction submission and a
monotonic native peer head; `db` and synchronization return immutable native
values by default.

**Focus:** Shared connection ownership; strict native connect/open; native
`db`/`try_db`/`sync`; transact/query/pull/entity/raw delegation at one captured
basis; explicit oracle/admin escape names; clone/cache/pin ownership; API
exports and examples.

**Completion signal:** One ordinary application workflow uses only
`Connection`, never increments compatibility materialization, shares head
progress across clones, and keeps old captured values exact across transaction,
consolidation, cache eviction, reconnect, and PostgreSQL restart.

### 4. Complete opt-in transaction reports

**Status:** Pending.

**Outcome:** Connection reports have exact native db-before/db-after,
tx-data/tempids and recovered submitter-before-observer/source ordering without
hidden retention while disabled.

**Focus:** One canonical report type/sequencing owner; per-row immutable tail
successors; atomic live-state publication before visibility; queue creation,
drain/removal and pin metrics; own/external transactions; tail failure and
retry; avoid duplicate reports between service and peer adoption.

**Completion signal:** Long catch-up retains no reports while disabled;
enabled own/external reports appear exactly once in source order with queryable
before/after values; a corrupt multi-row tail changes neither state nor queue;
removal releases queued pins and prevents future retention.

### 5. Index, schema, and excision synchronization

**Status:** Pending.

**Outcome:** Connection wait operations distinguish transaction progress from
physical root/schema/excision completion and wake on the exact publication or
generation event.

**Focus:** `sync_index`, `sync_schema`, `sync_excise`; basis plus physical
publication revision; pending AVET coverage; completed generation adoption;
timeouts without holding updater/I/O locks; restart/reconnect behavior.

**Completion signal:** Real-PostgreSQL tests prove same-basis root replacement
wakes index waiters, pending AVET blocks only the documented physical access
until `sync_schema`, excision waits for adopted completion, and timeout/restart
cannot deadlock ordinary lazy reads.

### 6. Public-boundary closure and parent fold-back

**Status:** Pending.

**Outcome:** Native immutable values and one connection are the truthful
ordinary API; eager compatibility is explicit and no claimed source behavior
is missing behind secondary methods.

**Focus:** Search/audit public call paths; source/deviation map; API compile
witness; differential/property tests; real PostgreSQL concurrency/restart;
format/check/Clippy; update Goals 9 and 0.

**Completion signal:** The full Goal 17 acceptance matrix passes without eager
materialization or collecting reverse traversal, prior Goal 16 guarantees
remain green, documentation names the deliberate Rust/PostgreSQL deviations,
and the parent loop can advance to Goal 18.

## Exit condition

Goal 17 completes only when ordinary applications use one cloneable native
connection to transact and observe exact immutable values; complete opt-in
reports, lazy bidirectional raw indexes, all documented time-point forms, and
index/schema/excision waits work across real PostgreSQL synchronization and
restart; and eager reconstruction is confined to explicitly named
oracle/administrative APIs.
