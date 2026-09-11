# Datomic → Atomic source atlas

This is the development entry point for source study, not a second set of user
instructions. The [plan](../../goal-0/0-plan.md) owns stage status. The
[inventory](inventory.tsv) accounts for all 493 Clojure/Java source files at
unannotated revision `cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`.

Run `python3 development/source/inventory.py --check` to verify the inventory and
that source edits only add full-line study comments. `--write` mechanically
regenerates the inventory. Hashes and counterpart comparisons describe the
baseline, not the annotated checkout. Reading-group hints are navigation aids
inferred from names, **not verified semantic ownership or completed coverage**.

## How to read the corpus

- `peer/` and `transactor/` record packaging. Shared mechanisms belong to shared
  Rust code even when both artifacts contain them. Same-path but nonidentical
  recovered files need comparison; generated identifier differences alone do
  not establish semantic differences.
- Exact dependency sources keep their original author/library attribution.
  The priority-map dependency is used by core.cache, reached from core.memoize
  and tools.analyzer. Datomic's cache factories instead use Caffeine here.
  Neither fact proves which paths a specific workload executes. Dependencies
  are valuable design evidence without being a mandatory native port list.
- `ATOMIC-NOTE` comments identify added development reasoning. Observed behavior,
  documented contracts, inferred rationale and unknowns are kept distinct. Code
  bodies and original documentation remain unchanged. The inventory does not
  assert that every symbol has already been explained.

## First end-to-end slice

The [transaction-data companion](../../datomic_pro_docs/04_transactions/02_transaction_data.atomic.md)
maps individual Map Forms passages and related promises to concrete functions,
Rust owners and existing examples/regressions. Current pilot annotations cover:

| Step | Recovered mechanism | Current Rust mechanism / disposition |
|---|---|---|
| Submit ordinary data | Peer `api/transact`, `Connection.transactAsync`; transactor `transaction/create-procargs`, `read-message` | `edn_transaction` admission retains unresolved forms; `TransactionRequest` and connection submission. Retain representation/assessment separation. |
| Expand shorthand | Both `db/expand-map`, `ProcessInpoint`, `ProcessExpander`, `with-tx` | `transaction::expand::Normalizer`, `transaction::pipeline::assess_forms_with_clock`, then `transaction::assess`. Retain shared assessment; reconcile nested-map identity guards. |
| Serialize and compute | Transactor `update/processor`, `process-transaction` | Service's authoritative write lane calls the shared assessor. Preserve one transaction order, not a second eager evaluator. |
| Publish durably | `update/fressianer`, `writer`; `log/Log.append`, `write-tail-descriptor`, `claim-log` | Rust-owned log/receipt objects and conditional publication in `transactor::authority`. Retain durable-before-success invariant and stale-writer checks. |
| Deliver outcome | `update/block-notifier` waits for `:logged`; Peer `notify-data`, `await-tx-result` | Exact receipts, immutable before/after report, connection observation. Retain Atomic's durable keyed-retry contract explicitly; source request correlation alone does not prove the same contract. |
| Read a stable value | Peer `api/db`, `Connection.db`, `Db.acceptDataCheck`, `query/q*` | Capture a `DatabaseValue`; local query/indices, no transaction-server query execution. Retain immutable root/overlay ownership. |
| Speculate | Peer `api/with`, `db/with-tx+opts` | `DatabaseValue::with_forms` uses the same transaction machinery without publication. Retain, with independent expected-result cases. |

These are named-symbol locators; baseline lines in chapter companions remain
usable even as annotations shift current line numbers. Annotation coverage here
is pilot-only, not complete namespace coverage. Source counterpart notes can
share rationale after comparison; do not copy a recovered peer/transactor engine
twice into Rust.

## Responsibility map for the cutover

The pilot supports a **module-first** cutover. A crate-per-namespace workspace
would add packaging work without resolving the present coupling. Establish real
module ownership and dependency direction first; split build/application crates
only when that boundary can actually prevent an unwanted dependency. A peer
read must not require an active transactor. A deliberate public API facade is
not a reason to retain obsolete internal paths or forwarding compatibility modules.

| Owner | Responsibility | Evidence / current work to keep or inspect |
|---|---|---|
| `model` | Exact values, datoms, schema and identity | Datomic `common`, `db` and Attribute/Datom interfaces; existing Rust value/schema/identity logic. `datomic.data` supplies a serialization permutation table, not the value model. |
| `collections` | Generic path-copied maps and cache bookkeeping mechanisms | Priority-map composition and current `SharedMap`; no database IDs, SQL or cache admission policy. The transaction-locator-specific BTSet belongs to `index::recent`, not generic collections. |
| `index` | Persistent covering trees, recent indexes, overlays, ordered traversal and merging | Datomic `index`, `btset`, `db/IndexSet`; implemented [module owner](../../src/index/mod.rs) retains path sharing and selective cursors. Tree/recent APIs are `index::{tree,recent}`; root type re-exports remain intentional, with no old module aliases. |
| `transaction` | Forms, expansion, identity resolution and shared assessment | `db/expand-map`, `with-tx`; one current Rust normalizer/assessor for eager, speculative and durable paths. |
| `query` / navigation | Datalog relations/rules, planning, result shaping, Pull/entity traversal | `query`, `datalog`, `pull`; organize related helpers together and inspect actual join/index choices. |
| `peer` | Immutable read handles, connections/observation and reader cache policy | `peer`, `adopter`, `cache`; depends on shared engine and submission contracts, not writer worker ownership. |
| `transactor` | Admission, ordered write pipeline, durability acknowledgement and background-index coordination | `update`, `indexer`, `lifecycle`; separate service policy from shared assessment/log/tree operations. |
| `storage` | Opaque object I/O and conditional references, with a PostgreSQL provider | `kv_store`, `kv_sql`, cluster abstractions; keep current object/reference SQL and move engine policy to its true owner. |
| `operations` | Catalog lifecycle, retention/GC, excision, backup/restore and administration | `catalog`, `garbage`, `excise`, backup namespaces; current Rust operations use the same shared structures. |
| application adapters | EDN, explicit native calls, async/transport/CLI interfaces | Relevant public API and serialization boundaries; native equivalents, not JVM invocation or alternate engines. |

The dependency direction is application composition → peer/transactor/operations
→ shared model/transaction/query/index mechanisms → opaque storage interfaces
where I/O is needed. Generic collections stay below domain policy. Current
cross-dependencies are work to resolve, not proof this direction is already
enforced. Physical membership is finalized when its component is read and moved.

User docs will follow `00_start_here` through `09_optional`, including relevant
subchapters, with Rust API documentation replacing JVM-specific guides. Source
annotations and chapter-local trace companions remain development material.

## Findings to carry into the owning component

- **Nested maps (transaction, repaired):** source `expand-submap` uses an explicit `:db/id`
  before calling the anonymous-child guard. `has-unique-id?` tests identity
  uniqueness; the prose says “unique” more broadly. Native orphan prevention now
  applies only to anonymous children; explicit IDs still undergo ordinary ID,
  type and identity validation. Retain native unique/value anonymous admission
  with conflict rather than upsert semantics, and native reverse nested maps.
  All 11 EDN transaction and four CLI tests pass with real PostgreSQL, including
  explicit nested updates, held values and restart/exact retry.
- **Write pipeline (transactor, explicit adaptation):** recovered processing, encoding, log writing
  and notification are pipelined and log writes can batch. Current Rust performs
  assessment/encoding/publication synchronously in its write lane. This preserves
  ordering but is not algorithmic/concurrency equivalence. The
  [ACID scheduling decision](../../datomic_pro_docs/04_transactions/05_acid.atomic.md#acid-scheduling-retain-one-fresh-transaction-authority-expose-the-tradeoff)
  keeps one uncertain-outcome state instead of a queue of assessed, uncommitted
  successors. Native exact receipts remain receipt-first. Guarded object uploads
  now share a bounded persistence barrier; complete service calls fell from 114
  to 32, with 17 replay calls unchanged. No controlled latency ratio or source
  throughput parity is claimed. Rust does not make pipelining unnecessary.
- **Caches (shared collections, repaired):** `collections/lru.rs` supplies keyed
  recency with a hash lookup and reusable linked slots. Tree, program and fulltext
  caches retain their own admission, byte/entry limits, counters and shared
  payloads. Cache history is not database history: local mutation is intentional.
  This preserves Atomic's LRU policy, not Caffeine admission-policy equivalence.
  The path-copied AVL map is retained in `collections/persistent_map.rs`, with no
  forwarding module at its old path. Benchmarks and limits are in the goal plan.
- **URI identity and routing (model/index, repaired):** logical URI comparison
  and hashing now use components while canonical spellings and request identity
  remain exact. The recovered `index/mindiff` only shortens String/vector values;
  URI separators therefore retain exact values, including inside tuples. Raw
  text prefixes were unsound for numeric ports (`:9 < :10` but raw prefix `:1`
  is below `:9`). The [index companion](../../datomic_pro_docs/06_indexes/01_index_model.atomic.md#im-routing-sparse-keys-must-obey-the-value-comparator)
  records the fix and explicit parent-byte cost; this is not JVM wire parity.
- **Retained reads (docs):** reconcile README with the current explicit retention
  grace and lack of reader pins; do not promise that a held handle overrides GC.
- **Lifecycle (selected Stage 3 coverage):** process heartbeat takeover and the
  per-database log claim are separate source fences. Frozen novelty, index CAS,
  current-tree excision rebasing, live-tail adoption and garbage marking have
  now been traced through their actual callers. Native `transactor::authority`
  retains the canonical writer; shared recovery/index/excision candidates do
  not start it. Administrative excision authority belongs to
  `transactor::excision_operator`, not `storage::excision`. Retain native atomic
  generation/receipt publication and epoch-protected content reuse explicitly;
  source separate index/log publications and best-effort mark buffering are not
  interchangeable protocols. The [HA](../../datomic_pro_docs/08_operations/01_capacity_and_reliability/01_high_availability.atomic.md),
  [excision](../../datomic_pro_docs/09_optional/02_specialized_operations/02_excision.atomic.md)
  and [reclamation](../../datomic_pro_docs/08_operations/00_architecture_and_storage/00_storage_services.atomic.md#atomic-note-lifecycle-reclamation-boundary)
  traces record exact symbols, costs and limitations. Unknowns remain all
  cross-process GC/restore schedules, reconstruction of omitted garbage marks,
  arbitrary canonical corruption and platform-specific failover. This is not
  complete namespace, backend or deployment coverage.

## Verification discipline

Source preservation and inventory checks run without the JVM. Comments are
reviewed against actual bodies; passing the preservation guard does not prove
their explanations correct. Product checks use independent documented examples
as well as shared-engine consistency tests. The plan records commands actually
run and their environment. A fixture returning early without PostgreSQL is not
a successful durability, failover or application check.

Shared structure traces continue in the chapter-local index/caching companions.
The [SQL provider trace](../../datomic_pro_docs/08_operations/00_architecture_and_storage/00_storage_services.atomic.md)
records the small provider boundary and an actual doc/source discrepancy in
connection validation queries. The [ACID companion](../../datomic_pro_docs/04_transactions/05_acid.atomic.md)
compares guarded multi-reference publication and protection epochs with their
callers: retain opaque guards and monotone stamps, with reachability, retirement
and acknowledgement policy in Rust. Selected lifecycle mechanisms and their
explicit adaptations are now traced above; broader operations and failure
coverage remain with their owning components. Small DDL alone is not an
architectural acceptance claim.
