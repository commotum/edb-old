# Goal 16 — Tiered Production Transactor

## Objective

Replace the production writer's retained eager `Database` with a bounded,
immutable, tiered database value that uses the same authenticated PostgreSQL
roots, recent log tier, metadata projection, and lazy ordered access as peers.
Keep the eager kernel as the executable semantic oracle, but make ordinary
transaction expansion, assessment, validation, state commitment, publication,
reports, restart, and failover correct without storing or rebuilding all
current/history information in transactor memory.

## Constraints

- `datomic_pro_docs` is the semantic authority. Recovered Datomic Pro 1.0.7705
  `Db`, `ProcessInpoint`, assessment/add-data hooks, persistent index, indexer,
  log-tail, and update lifecycle are the default representation and algorithmic
  blueprint.
- Rust throughout; PostgreSQL is the only durable store. Reuse its concrete
  log-generation, tree, fence, idempotency, and publication contracts. Do not
  introduce a generic storage backend.
- Preserve `Database::with` as a pure eager reference transition. Production
  code may share its semantic rules, but the oracle may not remain hidden in a
  writer cache, report, recovery path, or per-commit full materialization.
- Preserve declarative unordered atomic transactions, collision-free identity,
  successor-schema validation, current/history/time-view semantics, temporal
  persisted functions and predicates, acknowledged/unknown outcomes, and one
  database-bound fenced writer.
- Schema and idents remain information-derived resident metadata. Persistent
  trees and semantic commitments remain derived from the authoritative log;
  content is visible only after conditional publication.
- Memory and I/O claims require measurements. Count decoded nodes/ranges,
  retained recent bytes, cache bytes, semantic commitment work, and writer
  database residency independently of allocator noise.
- Do not pull Goal 17's general deployment harness, new query surface, generic
  HA orchestration, lifecycle redesign, or storage portability into this goal.
- A PostgreSQL test counts only when configuration is present and the database
  work actually executes. Preserve historical migrations byte-for-byte.

## Known context

- Goals 10–15 establish the semantic kernel, one fenced transaction service,
  temporal program behavior, authenticated persistent trees, exact lazy
  `DatabaseValue` reads, and safe PostgreSQL lifecycle operations.
- `src/postgres.rs::PostgresStore` still retains
  `BTreeMap<String, (Digest, Database)>`; a cache miss calls eager `recover_to`,
  and commit assessment/reporting uses `Database::assess_with_context` plus a
  full eager `db_after`. This makes production residency scale with total
  current and retained history despite the bounded peer/index architecture.
- `DatabaseValue` already provides the exact eager/native read seam for query,
  pull, entity, lookup, temporal, and filtered reads. It currently exposes
  owned `Vec<Datom>` reads and no transaction-mutation contract.
- `RecentTier`, `PeerSnapshot`, `PostgresTreeStore`, metadata replay, and the
  persistent semantic treap provide working pieces, but their ownership is
  peer/eager specific. The semantic commitment update is logarithmic only
  while its complete treap is retained inside eager `Database`.
- Goal 9 evidence item C25 maps the core gap. C08, C16, C24, C26, C29, and C31
  constrain the durable-base, metadata, recent-index, and publication design.

## Source blueprint

- The docs require databases larger than memory, immutable/cacheable index
  segments, and a bounded recent memory tier rebuilt from the log after the
  durable basis (`00_start_here/00_introduction.md:79-109`;
  `06_indexes/01_index_model.md:79-105`;
  `06_indexes/02_background_indexing.md:13-25`).
- Recovered `datomic.db.Db` separates `memidx`, `indexing`, `mid-index`, durable
  `index`, `history`, `memlog`, schema/idents, basis, and temporal/filter state
  (`peer/src-clj/datomic/db.clj:4719-4744`). `Db.seek*` merges tiers lazily
  (`:5035-5074`), `acceptDataCheck` applies a transaction to persistent recent
  sets (`:4748-4806`), and `prepare-for-indexing` moves the memory tier rather
  than copying the full database (`:5599-5610`).
- Recovered transaction processing resolves input against db-before, assesses
  the complete information set, constructs db-after, and then runs hooks and
  predicates (`db.clj:4181-4486,4807-4965,6525-6607,7451-7925`). The native
  design must preserve that control flow through lazy exact reads, not replace
  it with a partial-validity shortcut.
- `IndexerImpl` accounts memory/indexing bytes and enforces a hard limit
  (`transactor/src-clj/datomic/indexer.clj:262-329`). Startup adopts a durable
  root and reduces only its authoritative tail (`update.clj:2793-2857`;
  `log.clj:1406-1520`).

## Stages

### 1. Exact source map, residency baseline, and semantic access contract

**Status:** Complete 2026-09-03. `ARCHITECTURE.md` maps every eager owner,
reconstruction path, transaction read set, and recovered tier boundary. The
exact `DatabaseValue` seam now exposes frontier/last-instant metadata and a
fallible lazy current-prefix cursor implemented by both the eager oracle and
native root-plus-recent value, with no materialization method.

**Outcome:** The current eager production dependency is completely mapped and
measurable; one minimal exact read/metadata interface covers every operation
the transaction pipeline actually needs.

**Focus:** Call graph from service to store/kernel/publication; recovered `Db`
field/control-flow map; current/report/cache ownership; range and metadata
requirements for tempids, lookup refs, uniqueness, cardinality, CAS,
retractions, schema hooks, functions, predicates, excision, and commitment;
writer residency/node-read instrumentation; adversarial baseline.

**Completion signal:** An executable long-history witness demonstrates the
current unbounded eager behavior; the plan names every eager dependency and a
small Rust access contract can run against eager and native values without a
fallback `materialize()` escape hatch.

**Established evidence:** `tests/transactor_residency.rs` actually ran against
PostgreSQL 15.11 after 64 commits and exposed one cold recovered eager writer
value whose current/history counts grow with the database. The focused native
cursor witness passed against the same server, matched the eager result, read a
bounded tree path, and left compatibility-materialization counters at zero.
The cursor slice is commit `e3468c8`; its isolated gates were 137 library tests
passed (one subprocess helper ignored) and warning-denying all-target Clippy.

### 2. Lazy transaction assessment with eager-oracle equivalence

**Status:** Complete 2026-09-04. The production writer now runs the exact
prefix-driven assessor, persisted programs and predicates, complete successor
overlay, and schema/ident projection against one immutable native db-before.
Generated and adversarial PostgreSQL differentials cover ordinary
updates/upserts, CAS/conflict rejection, recursive retract-entity, schema
install/alter, all four indexes, ident alias repurposing, stored numeric
identity, AVET enable/disable, and restart. No production assessment path
materializes or retains the eager oracle.

**Outcome:** Transaction expansion and complete successor validation operate
over exact lazy ranges plus a bounded overlay representing db-after.

**Focus:** Stable db-before source; structural normalization; issued-ID and
tempid resolution; AVET uniqueness/upsert; EAVT cardinality/CAS/retract;
schema/ident hooks; tuple validation; tx time; nested persisted functions;
attribute and entity predicates; transaction-local current/history overlay;
explicit bounded work accounting.

**Completion signal:** Generated and adversarial transactions produce the same
datoms, tempids, errors, schema/idents, reports, and state digest as
`Database::with` across eager and native bases, including schema changes and
persisted behavior, without whole-index reads.

**Established evidence:** `tests/transactor_differential.rs` passes both the
curated restart sequence and the fixed-seed generated sequence against real
PostgreSQL. The unit differential/scaling corpus exercises early cursor
termination, grouped identity and uniqueness work, localized schema and tuple
validation, predicate roles, persisted reads, and exact rejection codes.
`tests/transactor_residency.rs` proves a localized transaction after 128
commits reads fewer than 128 source datoms and tree/commitment nodes rather
than scanning the database.

### 3. Durable incremental semantic commitment and publication

**Status:** Complete 2026-09-04. The PostgreSQL-resident version-2 semantic
treap verifies immutable content-addressed paths, removes the original asserted
coordinate named by a retraction, writes only successor paths, and publishes
the new coordinate in the same fenced SQL transaction as log content,
idempotency outcome, and head CAS. Missing/corrupt proofs fail closed, failed
publication remains invisible, and exact retry reuses the durable decision.

**Outcome:** The writer derives the authoritative successor commitment and
durable transaction from changed facts/ranges without retaining the eager
semantic treap or hashing/materializing the complete database.

**Focus:** Authenticated commitment nodes/root or equivalent exact persistent
set update; transaction-local proof reads; content-addressed writes before
head CAS; versioned representation; migration/compatibility boundary; corrupt
or missing proof failure; retry/idempotency interaction; root and tail
coordinates.

**Completion signal:** Localized commits touch work proportional to their
changed semantic paths, reproduce the eager commitment exactly, remain
invisible on failed publication, survive restart, and reject coherent-forgery
or missing-node cases.

**Established evidence:** persistent-commitment unit/PostgreSQL witnesses
match the eager version-2 root, exercise child-first writes and bounded path
visits, and reject corruption. The private publication/process-death tests
prove rollback and ambiguity behavior. Deep integrity additionally rejects a
structurally valid, internally coherent native tree whose facts were not
derived from the named authoritative log value.

### 4. Bounded tiered writer state and handoff

**Status:** Complete 2026-09-04. `WriterState` owns a `TieredSnapshot`, exact
commitment coordinate, and physical publication revision. Activation selects
an authenticated native root, streams/authenticates the log tail once, and
installs bounded recent/schema/ident/cache state. Consolidation can replace a
same-basis physical root and release the covered recent prefix. The old
native-tree-blind genesis replay reproduced by this stage no longer occurs on
an established writer path.

**Outcome:** One writer database value owns resident schema/idents, a bounded
recent tier, indexing handoff state, lazy durable roots/cache, log endpoint,
and the incremental commitment root—never the complete eager history.

**Focus:** Replace `PostgresStore.current`; base-plus-tail activation;
immutable successor sharing; recent soft/hard limits and backpressure;
indexing/consolidation handoff; cache ownership; report shape without retained
`Database`; same-basis repaired roots; generation activation/excision/restore;
bounded standby catch-up.

**Completion signal:** A history substantially larger than configured recent
and cache limits leaves measured writer residency bounded; local transactions
read bounded ranges; consolidation advances the durable basis and releases old
recent state; cold restart reconstructs root plus tail rather than genesis.

**Established evidence:** The 128-commit residency witness reaches an empty
recent tier after consolidation, retains zero eager database/current/history
values, stays within the configured 64-entry/1-MiB cache and fixed eight-root
decoded bound, then cold-opens exactly at the durable basis with zero tail
transactions. With the cache disabled, the next localized write records
bounded nonzero SQL path reads and commitment path-copy work. A separate
32-transaction over-cap recovery witness performs one streamed tail range read
and rejects before installing an oversized live writer state; immutable older
receipts remain readable after limits are lowered.

### 5. Fencing, failover, differential, and fault closure

**Status:** Complete 2026-09-04. The tiered value remains behind the same
database-bound epoch fence, durable request identity, unknown-outcome
reconciliation, response/report ordering, and index-first backpressure as Goal
12. Root repair, contention, standby takeover, corrupt tail/root rejection,
same-basis physical replacement, server restart, and old immutable values all
preserve a single exact history.

**Outcome:** The tiered transactor preserves every Goal 12 durability and
service guarantee under contention, retry, process failure, root replacement,
and standby takeover.

**Focus:** One served database/epoch; unknown-outcome resolution; no
post-acknowledgement fallibility; concurrent ordinary transactions; corrupt
base fallback; abrupt writer death; failover equivalence; old immutable peer
values; source/deviation ledger and operator observability.

**Completion signal:** Real PostgreSQL long-history, contention, restart, and
failover witnesses show no forks, gaps, duplicate commits, eager writer value,
or residency growth with total history, while reports and peer values agree
exactly with the eager oracle.

**Established evidence:** Fresh PostgreSQL runs pass the service failover,
recovery, backlog-liveness, differential, and residency suites. The complete
peer suite passes 18/18 and the complete GC suite passes 15/15 on pristine
databases. An explicitly restart-enabled peer witness and a separate
restart-enabled transactor recovery witness both stop and restart PostgreSQL
15.11, reopen the exact committed basis, and observe zero compatibility
materializations before the explicit oracle comparison. Migration 22 closes a
real retirement/build-intent race found by the serial GC suite and heals an
already stranded version-21 catalog.

### 6. Goal closure and parent fold-back

**Status:** Complete 2026-09-04. The implemented/source/deviation map is in
`ARCHITECTURE.md`; Goal 9's ledger records the corrected comparator, physical
AVET readiness, admissible `noHistory` projection, writer tiering, and the
remaining public/API work owned by Goals 17--20. Ordinary established-history
service activation, transaction, retry, failover, report, and background-index
paths contain no eager `Database`, `recover_to`, compatibility materializer, or
whole-index fallback. The fixed basis-0/1 creation bootstrap and explicitly
broad administrative recovery/inspection paths remain documented exceptions.

**Outcome:** Goal 16's bounded-writer claim is reproducible and narrowly
documented, with no eager fallback disguised behind compatibility APIs.

**Focus:** Warning-denying Clippy/format; source-backed architecture/deviation
record; non-skipping test matrix; measured thresholds; API and runbook truth;
Goal 9/Goal 0 status updates.

**Completion signal:** Every earlier stage has executable evidence, an
independent search/audit finds no production path retaining or rebuilding an
eager full database, and the parent loop can proceed to Goal 17.

**Established evidence:** Format, all-target check, warning-denying Clippy,
the 219-test library binary (218 passed, one explicit subprocess helper
ignored), and focused non-skipping PostgreSQL suites pass. `git diff --check`
is clean. The independent architecture audit accepted the tiered-writer shape
and kept deep portable-backup equivalence, public peer materialization, native
reverse/time views, tuple-ref input, and query-surface truth explicitly open in
Goals 17--20 rather than hiding them in this completion claim.

## Exit condition

Goal 16 completes only when the authoritative PostgreSQL writer processes and
publishes fully valid transactions from an immutable durable root plus bounded
recent/indexing tiers; its memory and range I/O follow configured bounds rather
than total database history; restart/failover and acknowledgement semantics are
unchanged; and generated eager/native differential tests establish exact
semantic equivalence.
