# Goal 4 — Persistent Indexes and Peer Read Architecture

## Objective

Build the native Rust peer/read layer over the completed kernel and PostgreSQL durability boundary. Independent peers must synchronize monotonically from PostgreSQL, retain immutable database snapshots, cache immutable index data safely, and answer raw EAVT, AEVT, AVET, VAET, history, `as-of`, and `since` reads locally. Add a persistent base-plus-recent index design and background consolidation without making derived indexes part of transaction truth or weakening Goal 3 recovery.

This goal realizes Stage 4 of `goal-0/0-plan.md`. It establishes the indexed database-value substrate that Goal 5 query and pull will consume; it does not implement Datalog, pull, production transaction-service coordination, HA leadership, or lifecycle operations.

## Constraints

- Use Rust throughout and PostgreSQL as the only durable store. Do not introduce a generic storage interface or preserve another backend.
- Treat `datomic_pro_docs` as semantic authority, the completed Goal 1–3 contracts and tests as executable boundaries, and `1.0.7705` as the default blueprint for database values, indexes, log tailing, peer synchronization, caches, immutable trees/segments, and connection state.
- Trace the relevant recovered `datomic.db`, `datomic.index`, `datomic.btset`, `datomic.db-io`, `datomic.valcache`, `datomic.log`, peer connection, and Java `Database`/`Connection` shapes before fixing physical representations or control flow. Preserve useful boundaries and performance choices through idiomatic Rust.
- Keep Goal 3 transaction rows and published head authoritative. Persistent indexes, roots, progress, caches, and consolidation outputs are derived, independently verified, safely rebuildable state; they must never make an uncommitted transaction visible.
- Preserve immutable database values and old snapshots. Advancing a peer creates a successor snapshot; it must not mutate a value already handed to a reader.
- Preserve exact index membership and ordering: EAVT and AEVT over all applicable datoms, AVET only where schema indexing/uniqueness requires it, VAET for refs, and correct current versus history behavior, including a deliberate resolution of `:db/noHistory` against the docs and recovered implementation.
- Synchronization must be monotonic, gap-free, basis-aware, restartable, and correct without notifications. PostgreSQL notification mechanisms may reduce latency only as hints.
- Make immutable cached values content-addressed and checksum/version validated. Eviction may cost performance but cannot alter meaning; corrupt reachable index data must fail closed or be discarded and rebuilt from the authoritative log according to an explicit rule.
- Keep Datalog, pull/entity navigation, full-text, persisted functions, transactor queues, backpressure, leader fencing/failover, backup, garbage collection, and production observability out of scope.
- Prefer direct behavior and differential evidence over frameworks, generalized service layers, or speculative scale machinery.

## Known context

- Goal 2 already provides immutable `Database` values, current/history EAVT, AEVT, AVET, and VAET roots, prefix/range access, temporal views, structural history sharing, and invariant checks. These straightforward roots are the semantic oracle for the production index representation.
- Goal 3 provides canonical `ATMC` v1 schema/transaction encoding, immutable chained PostgreSQL history, expected-basis/hash publication, durable idempotency, exact log recovery, and a verified transactor-side current-value cache.
- Goal 3 deliberately added no checkpoints or persistent read indexes. Its log and head remain sufficient to reconstruct truth; this goal may add derived index progress and checkpoint-like roots only to bound peer startup and read cost.
- The recovered Datomic shape separates immutable indexed bases from a recent transaction layer and lets database values name a basis while sharing immutable structures. Consolidation replaces physical roots without changing the logical database at that basis.
- Peers may progress at different rates. A peer's current connection database can advance while every previously returned database value remains stable and valid.
- Goal 5 will consume a local indexed source. This goal should expose complete, lazy-capable raw index traversal and snapshot semantics, not pre-empt the query planner or pull engine.

## Stages

### 1. Recovered index and peer contract

**Status:** Complete. `ARCHITECTURE.md` maps the recovered database/index,
base/recent, connection, watcher, cache, consolidation, corruption, and
`:db/noHistory` behavior to explicit Rust/PostgreSQL invariants. The sole
material physical deviation is using PostgreSQL-transactional immutable
manifests in place of a key/value-store root CAS.

**Outcome:** The logical/physical boundary, peer state machine, and derived-data invariants are explicit before persistent index formats become commitments.

**Focus:** Map documented database-value, index, cache, sync, transaction-report, history, and time-view behavior to the narrow `1.0.7705` paths; define base versus recent layers, authoritative versus derived state, snapshot identity, sync outcomes, consolidation visibility, cache trust, and corruption/rebuild policy; settle `:db/noHistory` semantics.

**Completion signal:** A concise contract names each peer/index state, basis invariant, publication boundary, cache rule, and failure outcome, with material deviations justified by Rust or PostgreSQL rather than convenience.

### 2. Versioned persistent index artifacts

**Outcome:** PostgreSQL can store and publish immutable, canonical, independently validated index segments and a rebuildable per-database index root/progress record.

**Focus:** Native versioned encoding for sorted index entries, segment bounds and identity, schema/basis coverage, content hashes, root manifests, and recent-layer metadata; one concrete PostgreSQL migration with immutable rows, constraints, checksums, and conditional derived-root publication; no index data in generic opaque storage abstractions.

**Completion signal:** Golden and malformed-format tests cover every index order and history form; real PostgreSQL tests prove immutable insertion, atomic root/progress publication, idempotent rebuild, and rejection or safe abandonment of corrupt and partial artifacts without changing the Goal 3 head.

### 3. Base, recent, and consolidation pipeline

**Outcome:** Committed log datoms become a bounded recent layer and immutable persistent index bases, and consolidation can replace physical structure without changing logical results.

**Focus:** Build all four current/history orders from authoritative transactions; incrementally apply a contiguous tail; choose segment boundaries using recovered evidence and measured Rust behavior; merge/retract correctly across layers; publish consolidation only for verified contiguous bases; preserve old manifests and snapshots; make interrupted work resumable and disposable.

**Completion signal:** Differential tests against Goal 2 show identical datoms and ordering before, during, and after consolidation; repeated and interrupted builders converge idempotently; old roots remain readable; and peer startup/replay work is bounded by the published base plus recent tail rather than full history.

### 4. Independent peer snapshots and synchronization

**Outcome:** Multiple Rust peers can open a database, expose immutable snapshots, and advance monotonically to a requested committed basis through PostgreSQL alone.

**Focus:** Peer connection/current-state ownership; open from the latest valid derived root plus authoritative log tail; head observation; gap-free transaction application; `sync`/await-basis semantics; transaction-report observation needed by later APIs; disconnection, retry, restart, stale root, and concurrent consolidation behavior; wake-up hints that never carry correctness.

**Completion signal:** Independent peers can lag, await a target basis, catch up without gaps or regressions, restart from persisted roots, and continue serving previously acquired snapshots unchanged while newer connection snapshots advance.

### 5. Local index access and immutable caching

**Outcome:** Readers traverse peer-local snapshot indexes with the documented raw access semantics while immutable durable values are cached safely and efficiently.

**Focus:** Exact prefix, seek, reverse-seek, range, and lazy iteration over composite base/recent layers; current, history, `as-of`, and `since` database values; snapshot-local schema; content-addressed segment cache with bounded residency and permanent-safe reuse; concurrency and cancellation at reader boundaries, without query or pull logic.

**Completion signal:** The peer surface returns the same ordered datoms as the kernel oracle for every supported view and index operation; reverse and boundary cases are correct; cache hits, misses, eviction, and reload are observationally invisible; and concurrent readers require no global write lock.

### 6. Multi-peer, restart, fault, and scale verification

**Outcome:** The index/peer boundary has real evidence that its semantic and performance structure survives independent progress, consolidation, restart, and damage.

**Focus:** Real PostgreSQL multi-peer tests; long histories and churn; simultaneous readers and builders; process death at artifact/root publication points; stale and corrupt manifests/segments; missed wake-ups; PostgreSQL restart; old-snapshot retention; cache pressure; `:db/noHistory`; and differential coverage across all index orders and temporal views.

**Completion signal:** Multiple peers at different bases converge when asked, old snapshots remain exact, consolidation never changes results, corrupt derived state is detected and rebuilt or rejected by policy, restart avoids full replay when a valid root exists, and the full Goal 1–3 plus new Rust test/lint suites pass.

## Index and peer exit condition

Goal 4 is complete when independent Rust peers backed only by PostgreSQL can open and monotonically synchronize immutable database snapshots, answer complete raw index and temporal reads locally from a verified base-plus-recent representation, safely reuse and evict immutable cached artifacts, and survive restart or interrupted consolidation without gaps, forks, or result changes. The result must be demonstrated against real PostgreSQL and remain a derived read architecture over Goal 3's authoritative log; query/pull, HA transaction service, and lifecycle operations remain explicitly unclaimed.
