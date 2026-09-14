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

## Coverage and dispositions

[coverage.tsv](coverage.tsv) supplies one current-path family disposition for
each of the 493 inventoried entries. [passages.tsv](passages.tsv) supplies one
passage-family disposition for each of the 90 original Pro Markdown chapters;
chapter-local companions carry exact source symbols and focused checks. Neither
ledger counts lines, note markers or files as a percentage of semantic coverage.
Run `python3 development/source/inventory.py --check` to check their membership and
current local links; it does not execute the JVM or prove the prose correct.
`python3 -B -m unittest discover -s development/source -p test_inventory.py`
checks rejection of duplicate/missing/extra rows, missing targets and empty
dispositions. `--write` only regenerates the baseline inventory, never manual
dispositions.

The baseline consists of 317 recovered Datomic initializers, 89 recovered Java
sources, 84 exact bundled-dependency sources, two exact Datomic dependencies and
one exact packaged resource. There are 50 byte-identical artifact pairs, 111
same-path nonidentical pairs and 171 artifact-only entries. Exact dependency and
resource attribution is retained; duplicate pairs need one mechanism explanation,
not two native implementations. Nonidentical pairs share a responsibility map,
not an unproven whole-file equivalence assertion.

The ledgers distinguish implemented native counterparts, explicit native
adaptations, JVM/compiler/presentation support with no independent public native
feature, non-target providers/client servers, and version-specific repair or
distribution instructions. A `current_rust_owner` is a responsibility locator,
not a claim that the entire source file was ported. Source support families can
be covered through their consequential callers without adding line-by-line
comments to generated initialization/protocol expansion.

Major peer/read families now have source-to-contract traces: captured values
and observation, Datalog/results, Pull/entity navigation, fulltext, cache
composition, public facades, futures/errors, queue admission and reconnection.
See the [API/completion trace](../../datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.atomic.md),
[query execution trace](../../datomic_pro_docs/05_query_and_pull/01_executing_queries.atomic.md),
[fixed-read/cache disposition](../../datomic_pro_docs/09_optional/02_specialized_operations/00_read_only_connections.atomic.md)
and [client boundary](../../datomic_pro_docs/09_optional/00_pro_client/03_client_library_reference.atomic.md).
Writer/storage/operations families use the separate authority, storage, capacity,
backup and excision companions below.

Residual source uncertainties are bounded and explicit: arbitrary JVM
classloader/exception behavior, untested Caffeine/Valcache scheduling and vendor
backends, every cross-process failure schedule, and reference-version mismatches
are not claimed equivalent. In particular, the recovered future warning helper
exits after warning despite its metadata; `Util` has mutable null-input branches;
and the concrete peer queue uses blocking `put` despite broad immediate-future
wording. Native controls, owned values, cache validation and Busy admission have
their own contracts/tests; none requires reproducing those host quirks. This is
not proof that all internal helper symbols or optional Datomic products were
reviewed or implemented.

## End-to-end transaction slice

The [transaction-data companion](../../datomic_pro_docs/04_transactions/02_transaction_data.atomic.md)
maps individual Map Forms passages and related promises to concrete functions,
Rust owners and existing examples/regressions. The slice connects:

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
usable even as annotations shift current line numbers. This slice is not a
namespace-wide symbol checklist; the ledgers above account for surrounding
families and explicit non-counterparts. Source counterpart notes can
share rationale after comparison; do not copy a recovered peer/transactor engine
twice into Rust.

## Responsibility map for the cutover

The source study supports a **module-first** cutover. A crate-per-namespace workspace
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
| `query` | Datalog relations/rules, planning, query controls and result shaping | `query/{parse-query,load-query,q*,group-rel}`, `datalog/{sched-in-order,eval-query,qsqr}`; [native module](../../src/query/mod.rs) separates preparation, execution, source access, rules/dependencies, numeric work and projection. See the [paragraph trace](../../datomic_pro_docs/05_query_and_pull/02_query_reference.atomic.md). |
| navigation | Pull patterns, lazy entity traversal and index-Pull projection | `pull/{normalize-pattern,pull*,ea->v,ra->e,index-pull}` and `query/EntityMap`; [native module](../../src/pull/mod.rs) separates selectors, attribute access, iterative projection, transforms, controls, entity cache and index cursor. See the [Pull/entity paragraph trace](../../datomic_pro_docs/05_query_and_pull/03_pull.atomic.md). |
| search | Fulltext analysis, candidate composition/validation and derived immutable search pages | `fulltext/{search,search-iterable,build-index}`, `fulltext-index/update-fulltext`, `lucene` and immutable directory adapters; [native module](../../src/fulltext/mod.rs) separates search/controls, analysis, records/history delta and generic key-tree ownership. Provider I/O stays in `storage/fulltext`; [paragraph trace](../../datomic_pro_docs/05_query_and_pull/02_query_reference.fulltext.atomic.md). |
| `database_value` | Shared immutable values, windows, log/reference/hint adapters and invocation | `Db`, `windowed`, index seeks; [module](../../src/database_value/mod.rs) separates value/views, cursor, resolution, log and invocation. Peer capture, query/navigation and assessment consume this shared engine. |
| `peer` | Advancing observation and captured snapshot facade | `peer`, `adopter`, `db_io`; [live.rs](../../src/peer/live.rs) owns observation, [snapshot.rs](../../src/peer/snapshot.rs) fixed capture. Typed tree/fulltext/program and SSD cache policy remains with those owners, not a duplicate peer engine. |
| `transactor` | Admission, ordered write pipeline, durability acknowledgement and background-index coordination | `update`, `indexer`, `lifecycle`; separate service policy from shared assessment/log/tree operations. |
| `storage` | Opaque object I/O and conditional references, with a PostgreSQL provider | `kv_store`, `kv_sql`, cluster abstractions; keep current object/reference SQL and move engine policy to its true owner. |
| `operations` | Catalog lifecycle, retention/GC, excision and administration | `catalog`, `garbage`, `excise`; [module](../../src/operations/mod.rs) separates operator, inspection, indexing, reclamation, deployment, excision and projection. |
| `backup` | Coherent capture, repositories/points, offline reads, verification and restore | `backup`, `fsbackup`; [module](../../src/backup/mod.rs) separates capture/repository/point/snapshot/restore and inventory/authority/replay/projection verification. [Backup trace](../../datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.atomic.md). |
| `application` | Native Connection composition and async/transport adapters | [connection.rs](../../src/application/connection.rs) can retain embedded service ownership; [asynchronous](../../src/application/asynchronous/mod.rs) and [local/remote transport](../../src/application/transport/mod.rs) are adapters, not alternate engines. |
| `runtime`, `observation`, CLI | Generic bounded work, durable change consumption and command dispatch | [executor](../../src/runtime/executor/mod.rs) has no database semantics; [consumer/notices](../../src/observation/mod.rs) separate replay/checkpoints from wakeup hints; [CLI](../../src/bin/atomic/main.rs) composes application and operations APIs. |

The dependency direction is application composition → peer/transactor/operations
→ shared model/transaction/query/index mechanisms → opaque storage interfaces
where I/O is needed. Generic collections stay below domain policy. Current
cross-dependencies are not proof this direction is statically enforced. The
paths above are current ownership, not forwarding compatibility modules.

User docs now follow [00_start_here](../../docs/00_start_here/00_introduction.md)
through `09_optional`, with native Rust API chapters replacing JVM guides. Source
annotations and chapter-local trace companions remain development material.

## Findings to carry into the owning component

- **Fulltext (selected Stage 4 coverage):** the
  [fulltext trace](../../datomic_pro_docs/05_query_and_pull/02_query_reference.fulltext.atomic.md)
  distinguishes the source's asynchronous Lucene tier from native bounded
  recent/speculative completion, exact visible assertion identity and physical
  index lag. Native analyzer/query subset, BM25, limit-after-validation and
  authenticated path-copy pages are explicit adaptations. Rejected historical
  candidates now participate in direct/enclosing-query cancellation and work
  accounting. Existing lag, identity, cache, build-failure and restoration
  checks are retained; dead idle-retry bookkeeping has no behavioral contract.

- **Pull and entities (selected Stage 4 coverage):** the
  [navigation trace](../../datomic_pro_docs/05_query_and_pull/03_pull.atomic.md)
  separates component cardinality from expansion, schema-resolved underscore
  names from explicit direction, and lazy Entity identity from historical
  contents. Focused repairs retain id-only reverse component defaults, apply
  explicit transforms/defaults to unresolved entities, preserve identified
  reverse parents as entities, and interrupt index-Pull inside rejected filters.
  Native iteration/controls and Pro empty-result choices are explicit adaptations;
  source body preservation is not a semantic coverage percentage.

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
- **Retained reads (docs):** README and current user chapters state explicit
  retention grace and no reader pins; a held handle does not override GC.
- **Immutable capture and observation (selected Stage 4 coverage):** a connection
  advances while a captured Db retains its basis, layers and temporal bounds.
  Read values and window/log/reference machinery belong to `database_value`;
  live observation belongs to `peer`; optional embedded writer ownership makes
  native Connection application composition. The [filter trace](../../datomic_pro_docs/02_core_concepts/02_database_filters.atomic.md)
  and [deployment trace](../../datomic_pro_docs/08_operations/00_architecture_and_storage/02_datomic_deployment.atomic.md)
  distinguish capture, transactor barriers, target-specific completion and
  receipt-backed native observation. Named source paths/callers were read, not
  every peer namespace. Exact-report endpoint replay and public log iteration
  exposed separate complete-path cost gaps; repairs require actual reader-path
  counters, not inference from lower-level benchmarks. Native schema/index waits
  now inspect existing request-coordinate history for pending AVET work, retaining
  old partial checkpoints while excluding work admitted after the target.
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

- **Datalog and result projection (selected Stage 4 coverage):** source query
  preparation, explicit source dispatch, indexed/collection joins, QSQR answer
  sets, required bindings, negative completion, grouping and deferred projection
  are traced in the [query companion](../../datomic_pro_docs/05_query_and_pull/02_query_reference.atomic.md).
  The native implementation now lives under `query/`, with public result adapters
  beside their owning engine. `distinct` retains a public set through typed and
  readable EDN output; lazy Pull carries the original value-byte maximum as well
  as already-used bytes. Native bound-score scheduling, controlled full scans,
  exact arithmetic and explicit shared controls are retained adaptations, not
  source cost/rounding equivalence. Ordinary or-join exports are distinct from
  required head inputs despite broader prose in the Pro paragraph. Companion
  rows name focused checks; integrated release acceptance remains separate.

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
