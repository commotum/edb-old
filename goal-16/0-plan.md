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

**Status:** In progress. The exact prefix-driven assessor and bounded
transaction overlay are implemented in `bc4bcd1`/`ed0dc5d`; focused eager
oracle fixtures cover ordinary updates/upserts, CAS/conflict rejection,
recursive retract-entity, schema install/alter, all four indexes, ident alias
repurposing, stored numeric identity, and AVET enable/disable. Persisted
program execution now accepts the same exact value (`5239920`). Production
wiring and a generated native-PostgreSQL differential remain open, so this is
not yet the completion signal.

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

### 3. Durable incremental semantic commitment and publication

**Status:** In progress. Migration 15 and a PostgreSQL-resident version-2
semantic treap are under focused verification. Integration must remove the
old asserted datom named by each retraction (the commitment key includes the
original transaction coordinate), publish its successor coordinate in the
same SQL transaction as the log/head CAS, and prove rollback, corruption, and
bounded path work before this stage can close.

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

### 4. Bounded tiered writer state and handoff

**Status:** Pending. A concrete restart defect is now reproduced against the
unchanged Goal 15 baseline: `PostgresIndexer` publishes native
`atomic_tree_manifests`, while `recover_transactor_state` considers only
legacy `atomic_index_manifests`. Consequently failover after native
consolidation falls back to genesis replay (`RecoveryStats.base_t == 0`). The
repair must activate the same authenticated native root-plus-tail value used
by peers and must not route through the eager compatibility cell.

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

### 5. Fencing, failover, differential, and fault closure

**Status:** Pending.

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

### 6. Goal closure and parent fold-back

**Status:** Pending.

**Outcome:** Goal 16's bounded-writer claim is reproducible and narrowly
documented, with no eager fallback disguised behind compatibility APIs.

**Focus:** Warning-denying Clippy/format; source-backed architecture/deviation
record; non-skipping test matrix; measured thresholds; API and runbook truth;
Goal 9/Goal 0 status updates.

**Completion signal:** Every earlier stage has executable evidence, an
independent search/audit finds no production path retaining or rebuilding an
eager full database, and the parent loop can proceed to Goal 17.

## Exit condition

Goal 16 completes only when the authoritative PostgreSQL writer processes and
publishes fully valid transactions from an immutable durable root plus bounded
recent/indexing tiers; its memory and range I/O follow configured bounds rather
than total database history; restart/failover and acknowledgement semantics are
unchanged; and generated eager/native differential tests establish exact
semantic equivalence.
