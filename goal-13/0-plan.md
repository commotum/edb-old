# Goal 13 — Persistent Index Trees and Live Peers

## Objective

Replace the eager whole-database peer/index prototype with the Datomic-shaped
read architecture promised by Goal 0: shallow immutable persistent indexes,
a bounded recent transaction tier, lazy cache-backed seeks, incremental
affected-range consolidation, atomically advancing shared peer state, and
immutable snapshots that remain valid while the live connection advances.

The pure `Database` remains the semantic oracle. Production peers must stop
requiring all current and historical datoms in resident Rust collections or
rebuilding every index for a localized update.

## Constraints

- Follow `goal-9/EVIDENCE_LEDGER.md` C08, C09, C16, C17, and C24.
  `datomic_pro_docs` is the semantic authority; recovered `RootNode`,
  `DirNode`, `Leaf`, `Index.seek`, `merge-one-index`, `merge-db`, `Db.seek*`,
  connection-cache, and root/catch-up control flow in `1.0.7705` are the
  default implementation blueprint.
- Rust throughout, PostgreSQL only. Query immutable PostgreSQL content and
  publication tables directly; do not add a generic storage backend trait.
- Preserve four useful orders (EAVT, AEVT, AVET, VAET), current and retained
  history semantics, exact database-value identity, temporal/filter behavior,
  collision-safe stored-value ordering, and fail-closed canonical decoding.
- Treat the transaction log as authoritative and index trees as derived,
  replaceable values. Never let a derived root authorize or rewrite history.
- Keep `Database::with` pure and retain the existing in-memory implementation
  as an independent correctness oracle. A production peer may expose a
  different lazy snapshot type or internal access path where forcing the
  oracle's full vectors would defeat the goal.
- Structural sharing must be real and measurable. A localized update may
  rewrite affected leaf/directory paths and a bounded recent layer, not every
  historical leaf or every ordering.
- Cache immutable content by hash with explicit byte/count bounds. Do not
  confuse logical history retention with physical retention of every
  superseded root; Goal 15 owns final GC/grace policy, but this goal must expose
  safe pins/reachability.
- Do not pull Goal 14 query-language repairs or Goal 15 security/backup/excision
  policy into this child except where a minimal index access contract is needed
  to prove lazy correctness.
- Real PostgreSQL tests must prove they ran. Performance/space claims require
  measured counters or resident bounds, not comments or a tail-length field.

## Known context

- Goal 12 now adopts a verified base and replays only its authoritative log
  tail, but `recover_index_base` eagerly fetches every segment, reconstructs
  full current/history vectors, and rebuilds all in-memory indexes.
- `Database` stores complete ordered current roots and transaction-chunked
  history in process memory. `Peer::db` returns an immutable value, but the
  peer itself is a single mutable handle rather than a cloneable shared
  connection whose snapshots advance atomically.
- Current consolidation writes flat index segments from complete materialized
  orders. Content hashes and publication binding are useful foundations, but
  they are not a directory tree and localized commits still lead to global
  rebuild work.
- The Goal 12 semantic state commitment is correct but computed over all
  current plus retained history on every transaction. This goal owns replacing
  that scaling shortcut with a persistent/incremental commitment compatible
  with exact recovery and derived-base authentication.
- Goal 12 tests already cover corrupt-base fallback, no-history transition
  timing, base-plus-tail equality, old immutable values, and server restart.
  Preserve and strengthen them instead of replacing their oracle.

## Stages

### 1. Recovered architecture contract and red scale witnesses

**Status:** Complete 2026-09-03. `ARCHITECTURE.md` maps the documented and
recovered three-level tree, tier/adoption, cache, connection, commitment, and
GC boundaries; inventories every eager production path; assigns its owner;
and fixes structural counters and fault/concurrency/scale witnesses before the
representation changes.

**Outcome:** A concise source map fixes the native tree, recent-tier, seek,
merge, cache, connection, snapshot, pin, and commitment boundaries before the
representation changes.

**Focus:** Trace exact documentation and 1.0.7705 types/control flow; inventory
every eager materialization/rebuild; define canonical key/range/fanout rules;
add measured red witnesses for localized consolidation, base recovery reads,
bounded cache residency, long history, shared sync, and old snapshots.

**Completion signal:** The contract distinguishes semantic requirements from
implementation choices, every current O(N) path has an owner, and the red
witnesses fail because of observed eager work rather than missing scaffolding.

### 2. Canonical shallow immutable tree values

**Status:** Complete 2026-09-03. `src/persistent_tree.rs` implements the fixed
root/directory/columnar-leaf shape, canonical format-v3 encoding, compact
minimum-difference separators, hash-addressed children, exact count/range
validation, and logarithmic seek/range traversal in all four orders. Component
presence is encoded separately from numeric payloads: omitted sparse
components compare as lower sentinels, while native entity `0` remains a real
value. Eighteen adversarial unit witnesses cover corruption, missing children,
overlap, count and capacity faults, large values, tuples, one-datom leaves,
copy-on-write splits, and the VAET entity-zero/T-order regression.

**Outcome:** Versioned content-addressed leaf, directory, and root values
represent each index order with explicit key bounds, fanout, level, counts,
and hashes; malformed or noncanonical trees fail closed.

**Focus:** Rust node types; canonical encoding; stored-value comparisons;
bounded node sizes; range routing; streaming construction; PostgreSQL content
rows and root publication; exact current/history and no-history projection.

**Completion signal:** Generated and adversarial trees round-trip, logarithmic
seeks return the same ranges as the pure oracle in all four orders, node sizes
stay bounded on large inputs, and corruption/missing-child/range-overlap/root
substitution witnesses reject without changing authoritative state.

### 3. Incremental affected-range merge and recent tier

**Status:** Complete 2026-09-03. The native PostgreSQL indexer uses
the newest authenticated native root plus its contiguous log tail, rewrites
only affected root/directory/leaf paths, persists only candidate content, and
publishes the manifest/root set last. It no longer dual-writes the legacy flat
manifest. Exact current removals retain the old assertion tx; `:db/noHistory`
uses `RecentTier::no_history_pairs` at job time and never sweeps an old base.
AVET enable/drop is correctly treated as the recovered broad attribute-range
exception rather than mislabeled localized work.

Real PostgreSQL evidence in `tests/incremental_tree_consolidation.rs`:

- A 1,024-entity/854-node base followed by a three-datom localized update read
  83 nodes, produced 35 candidate nodes (31 new rows in that run), and reused
  832 untouched child references; all eight current/history trees matched the
  independent `Database` oracle. Already-authenticated nodes are retained
  across candidate selection and merge planning, and adjacent preloads touch
  only the predecessor-last/successor-first boundaries used by sparse-key
  repair. The original strict tenfold witness passes without weakening it.
- A failure after immutable-node insertion left no publication, a peer still
  read the prior root plus authenticated recent tail exactly, and retry reused
  the orphaned content without double publication.
- AVET false→true backfilled the affected attribute from AEVT and true→false
  removed that physical AVET range; both endpoints matched all oracle orders.
- An assertion and retraction wholly inside one noHistory tail consolidates
  without panic and filters only that eligible pair.

Sparse routing now treats omission as an explicit lower sentinel rather than a
zero payload. The real AVET false-to-true-to-false range job, including the
VAET system-entity-zero boundary that exposed the defect, passes.

**Outcome:** New transaction datoms enter a bounded immutable recent layer;
consolidation merges only affected ranges into persistent trees and publishes a
new root last while structurally sharing untouched content.

**Focus:** Datomic-style multi-tier seek union; add/retract collapse; localized
copy-on-write paths; split/merge rules; deterministic concurrent builders;
incremental semantic commitment; no-history cutoffs; interrupted publication;
work/byte/reuse counters.

**Completion signal:** Localized updates rewrite work proportional to changed
ranges and tree height rather than total history, unchanged segment hashes are
reused, interrupted/losing builders remain invisible, the recent layer stays
within its configured bound, and base+recent results equal the oracle.

### 4. Lazy immutable snapshots and bounded caches

**Status:** Complete 2026-09-03. `PeerSnapshot` retains immutable root/recent
state and `PeerIndexCursor` loads one directory/leaf path at a time. Cursors own
`Arc` node values and remain exact through connection advancement and forced
cache eviction. The shared hash cache accounts encoded bytes plus recursive
decoded allocations and is bounded by entry and byte limits. Native open,
refresh, metadata reconstruction, seeks, and sync do not create the eager
compatibility `Database`; measured PostgreSQL witnesses assert zero
compatibility materializations and bounded residency.

**Outcome:** A peer snapshot holds immutable roots plus recent layers and reads
only sought content through shared byte-bounded caches; creating or querying a
snapshot does not materialize unrelated index/history data.

**Focus:** Snapshot-local exact basis/view/filter state; lazy range iterators;
hash-keyed node cache; request coalescing; safe lock boundaries; cache metrics;
large values and histories exceeding cache; query/pull compatibility seam for
Goal 14.

**Completion signal:** Instrumented seeks fetch only relevant root-to-leaf
paths, repeated reads hit immutable caches, resident bytes stay bounded as
history grows beyond cache, eviction never changes results, and old snapshots
continue reading their pinned roots after consolidation and sync.

### 5. Shared atomically advancing peer connection

**Status:** Complete 2026-09-03. Cloneable `Peer` handles share one connection
core, serialized updater/I/O lane, bounded immutable-node cache, and atomically
published `Arc<PeerState>`. Complete root/tail/hash/frontier/generation state is
constructed before one swap; failures retain the prior value. Old snapshots
and open cursors remain usable, waiters do not hold the cache lane while
sleeping, and transaction reports are absent by default and removable when
enabled. Physical root revision advances independently from logical basis, so
a corrupt current root is repaired and adopted at the same idle basis.

**Outcome:** Cloneable peers share one monotonic connection state while every
captured database value remains immutable and independently usable.

**Focus:** `Arc` connection core; atomic generation/root/tail adoption; sync-to
waiting and cancellation; one fetch/build per generation; failure leaves the
previous value intact; transaction reports; snapshot/root pins; concurrent
readers and lagging peers.

**Completion signal:** Many clones observe monotonic bases, concurrent sync
coalesces without gaps or partial swaps, corrupt tail/root adoption preserves
the last good value, old snapshots remain exact, and readers do not require a
live transactor.

### 6. Recovery, measurement, and parent closure

**Status:** Complete 2026-09-03 for this child's boundary. Real PostgreSQL
gates pass for background indexing (5), persistent tree storage (2), affected
range consolidation (3), and native peer/restart/concurrency behavior (15),
including an actual `pg_ctl` restart. The 87-test pure library gate passes with
one intentional subprocess worker ignored; `cargo fmt --check`, all-target
compile, and warning-denying all-target Clippy pass. A migration-count fixture
was advanced to v12 and its deliberate missing-log-row corruption now uses
scoped replica mode because native manifest foreign keys correctly prevent an
ordinary delete.

The attempted repository-wide PostgreSQL run also produced honest red
downstream witnesses rather than hidden skips: excision/GC still assumes the
retired flat publication tables (Goal 15), and production transactor recovery
still expects a flat eager base (Goal 16). Those failures do not use the peer
architecture and are retained as the first executable evidence for their
owning children.

**Outcome:** Restart/failover uses lazy verified roots plus a bounded recent
tail, and measured evidence establishes the Goal 9 Stage 4 outcome without
smuggling eager work behind counters.

**Focus:** Cold/warm restart; root authentication; long history; tail catch-up;
cache-cold and cache-warm work; localized updates; PostgreSQL restart;
format/Clippy; architecture/deviation ledger and Goal 0 fold-back.

**Completion signal:** Real PostgreSQL tests show restart/catch-up equals the
pure model, base adoption does not read every leaf, localized consolidation is
sublinear with high unchanged-content reuse, cache/resident memory is bounded,
old snapshots and fail-closed corruption behavior survive restart, and all
pure/integration gates pass.

## Exit condition

Goal 13 completes only when production peer reads and recovery use immutable
shallow persistent trees plus a bounded recent tier lazily; consolidation and
state commitment are incremental for localized changes; shared peers advance
atomically while old snapshots remain valid; measured PostgreSQL evidence
establishes bounded residency, localized work, exact oracle equivalence, and
fail-closed restart behavior.

**Result:** Achieved 2026-09-03. The eager `Database` remains only the explicit
semantic/compatibility oracle. Query/pull still force that oracle through the
old public peer helpers; Goal 14 owns moving those consumers onto the now-live
snapshot access path, and Goal 16 owns removing the oracle from the writer.
