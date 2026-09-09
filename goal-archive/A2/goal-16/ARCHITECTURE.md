# Goal 16 final architecture and source map

Goal 16 replaced the production transactor's eager database cache with an
immutable native root-plus-tail value. `Database` remains the pure semantic
oracle and the explicit administrative reconstruction type. For established
history, an ordinary service activation, transaction, retry, report, or
failover neither retains nor reconstructs it. The bounded new-database
exception is basis 0/1: service startup may run
`consolidate_fresh_database`, reconstructing only fixed bootstrap/application
schema input to publish the first native root, then immediately opens and
retains the native value. Public `Peer` compatibility is not covered by this
writer claim: a database with no native publication can still use `recover_to`,
and `Peer::db` explicitly materializes an eager value. Removing that Goal
17-owned compatibility surface is separate from Goal 16 writer residency.
This document keeps the original dependency audit below because it explains
why the replacement boundary is as broad as it is.

## Implemented production shape

| Boundary | Native Rust implementation | Recovered/default blueprint |
|---|---|---|
| Writer value | `postgres::WriterState` owns a `TieredSnapshot`, persistent semantic-commitment coordinate, physical publication revision, and last-operation work. The snapshot pins one authenticated native root, a persistent bounded recent tail, resident schema/idents, and a bounded node cache. | `Db` separates `memidx`, `indexing`, `mid-index`, durable `index`/`history`, `memlog`, metadata, and endpoint coordinates (`1.0.7705/peer/src-clj/datomic/db.clj:4719-4744`). |
| Assessment | `tiered_assessor` normalizes and resolves against one exact `DatabaseValue`, memoizes repeated prefixes, groups identity/uniqueness/cardinality work with ordered maps/sorts, and reads only touched schema/entity/value ranges except documented broad schema operations. | `ProcessInpoint`, `get-ids`, `filter-assess-tx-datoms`, and keyed deduplication separate expansion from whole-information assessment (`db.clj:4118-4490,6525-6819,7051-7473`). |
| Proposed value | A transaction-local immutable overlay merges the complete tx data with db-before for successor checks and entity predicates. It streams point-current and history reads and is discarded after publication. | Transaction functions run against db-before; `addData` forms the complete db-after before hooks/spec checks (`db.clj:4807-4965,7451-7925`; transaction-model docs). |
| Metadata | Schema and ident projections are derived from ordinary datoms. Unchanged transactions share their `Arc`s; actual schema/ident changes derive one replacement projection. Repeated `:db.alter/attribute` events remain distinct history even when the hook value repeats. | `Db.acceptDataCheck` shares persistent values; recovered redundancy deliberately exempts attribute 19 (`db.clj:4748-4806,4854-4905`). |
| State commitment | A versioned content-addressed semantic treap is read and path-copied in PostgreSQL. Retractions remove the original asserted coordinate; changed leaves and proofs, not the complete set, determine the successor root. | This digest is Atomic-specific, while immutable content-first/root-last publication follows the recovered persistent-index discipline. |
| Durable publication | Transaction content, request outcome, commitment nodes/root, and head/epoch CAS share one PostgreSQL transaction. Installed writer state changes only after commit acknowledgement; ambiguity is resolved by durable request identity exactly once. | Datomic serializes transactions and publishes immutable content through a conditional root update; time has no order inside one transaction. |
| Recovery/failover | Activation opens the newest usable authenticated native root and reduces only its exact log tail. Missing late roots or tails above the configured hard bound return `service/native-index-required`; explicit `PostgresIndexer::consolidate` performs repair. | Startup adopts a durable root and catches up from the log; indexing is a separate bounded activity (`transactor/src-clj/datomic/update.clj:2793-2857`; `log.clj:1406-1520`). |
| Indexing pressure | Recent live bytes, frozen/publication backlog, and report-queue pressure are separately observable. Threshold crossing requests index work; hard pressure attempts index-first progress before rejecting new novelty. Existing authenticated tails remain readable after a configuration decrease. | `IndexerImpl` accounts per-database memory/indexing bytes and a process hard limit (`transactor/src-clj/datomic/indexer.clj:262-329`). |
| Reports | Service reports hold exact immutable native db-before/db-after values, tx data, and tempids. They may pin roots/generations, and queue/pin pressure is observable rather than disguised as eager database residency. | Transaction reports intentionally contain immutable before/after values and an opt-in queue may grow if not drained (`datomic_pro_docs/04_transactions/03_processing_transactions.md:25-59`). |

The logical read budget covers persisted-function expansion, normalization,
assessment, successor validation and predicates, and commitment predecessor
lookups. A shared immutable txInstant memo makes that coordinate one charged
read across clones and wrappers. Cache hits and transaction-local prefix hits
are reported distinctly; cache effectiveness never changes semantics.

Physical AVET readiness is separate from logical schema. A new
`:db/index`/`:db/unique` fact does not make old values magically present in an
older AVET root. The writer uses a correct AEVT fallback while the affected
root is not ready, and publication records when AVET becomes usable. Raw index
access deliberately preserves the recovered physical distinction: an
unqualified AVET scan contains only physically ready attributes, while an
attribute-qualified AVET seek fails explicitly until that attribute is ready.
Logical query planning tests readiness and falls back to the complete AEVT
attribute range, so backfill timing cannot change query answers. This matches
the docs' `sync-schema` availability boundary
(`datomic_pro_docs/03_schema/01_changing_schema.md:75-79`) and recovered
`Attribute.hasAVET`, qualified `seek-datoms`, and `add-avet` paths
(`db.clj:1019-1038,1880-1915,3382-3642`).

The source-corrected tree comparator is versioned as **ATIX v4**: logical
E/A/V, descending T, assertion before retraction, then a native
stored-representation tie-break (`1.0.7705/peer/src-clj/datomic/common.clj:123-172`,
`db.clj:885-930`, `index.clj:2522-2547`). Authoritative **ATMC v3**
datom-bearing values keep their already-published stored-first canonical order.
Administrative rebuilds decode that frozen format and write new ATIX v4 nodes;
neither format silently changes meaning.

Within the production writer/service path, full reconstruction is deliberately
confined to explicit broad work: the bounded basis-0/1 first-root build,
migration/backfill, deep verification, excision, and backup/restore. The
Goal-17-owned public peer compatibility exceptions described above are not a
writer fallback. In particular, if every derived request-base tree is corrupt,
backup may rebuild a source-admissible portable projection from the
authenticated immutable log at the exact semantic coordinate. Its retained
history need not be byte-for-byte identical when `:db/noHistory` applies,
because removal and indexing-job timing are expressly nonsemantic
(`datomic_pro_docs/03_schema/00_schema_data_reference.md:201-213`,
`03_schema/01_changing_schema.md:62-73`).
That is not a writer fallback: the docs explicitly say backup memory is
proportional to database size, while the introduction makes the log
authoritative and index trees replaceable
(`datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md:18-31`;
`00_start_here/00_introduction.md:79-109`).

## Semantic constraints

- A database is an immutable value and a transaction is a declarative,
  unordered, atomic function from db-before to db-after
  (`datomic_pro_docs/04_transactions/01_transaction_model.md:13-48`).
- Transaction functions see db-before; attribute predicates receive the
  asserted value under the predicate definition installed in db-before; entity
  predicates see the complete db-after while their spec definition is selected
  from db-before. Newly installed predicate/spec definitions take effect on a
  later transaction. None may observe an intermediate transaction state
  (`04_transactions/01_transaction_model.md:44-48`,
  `04_transactions/05_acid.md:31-35`).
- A database and its indexes may exceed memory. Durable immutable tree
  segments are combined with a recent in-memory tier rebuilt from the log
  (`00_start_here/00_introduction.md:79-109`,
  `06_indexes/01_index_model.md:79-105`).
- Idents are resident derived information. Old names remain aliases and a
  later assertion can repurpose an alias; retractions do not erase the name
  from the historical ident map (`04_transactions/02_transaction_data.md:286-324`,
  recovered `db.clj` key hook, and `src/idents.rs:5-73`).
- Exact reads from db-before are fundamental transaction work, not a defect:
  identity, tuples, uniqueness, cardinality, redundancy, functions, and
  predicates all require them. Reads bound latency, and Datomic therefore
  plans/prefetches them (`04_transactions/08_transaction_hints.md:8-30`).
- Adding AVET to an attribute that already contains values and excision are
  legitimate broad indexing operations; ordinary commits are not
  (`06_indexes/02_background_indexing.md:27-36`, recovered
  `db.clj:3457-3606`).

## Pre-repair eager dependency audit

The following table records the state found at the start of Goal 16. Its
“current Rust behavior” column is historical; the final disposition is the
implemented shape above.

| Boundary | Current Rust behavior | Consequence | 1.0.7705/default correction |
|---|---|---|---|
| Writer owner | `PostgresStore.current` owns one `(Digest, Database)` per database (`src/postgres.rs:2118-2123`). | Complete current facts, complete history chunks, four current/history indexes, and the semantic treap remain reachable. | Recovered `Db` owns durable roots plus `memidx`, optional frozen `indexing`, `mid-index`, `memlog`, metadata, and basis coordinates (`db.clj:4719-4744`), not a flat materialization. |
| Creation | `create_database` creates eager application and bootstrap databases, hashes them, returns one, and inserts it into `current` (`postgres.rs:2428-2626`). | Bounded at creation, but establishes the wrong public/owner type. | Creation may use the eager oracle for fixed bootstrap/schema input, but the published result and installed writer state must be a tiered immutable value. |
| Cold `recover` / cache miss | `recover`, `recover_basis`, and transaction cache misses call `recover_to` and cache/return its eager result (`postgres.rs:2629-2664,3063-3068`). | Restart and an evicted writer replay from genesis and scale with all retained history. | Load one authenticated durable root and metadata projection, then authenticate/reduce only `(durable_base_t, head_t]`. Recovered startup does this in `update.clj:2793-2861` and `log.clj:1406-1520`. |
| Lease activation / failover | `activate_transactor_state` calls `peer::recover_transactor_state`, which still returns `Database` and is inserted in `current` (`postgres.rs:2212-2261`; `peer.rs:4677-4737`). | A standby takeover is an eager full recovery even when native roots exist. | Activation must install root + bounded tail + metadata + commitment root under the verified epoch. No compatibility materialization counter may move. |
| Transaction entry | `transact_generated` clones `current`, or eagerly recovers the head, and passes `&Database` to generator, normalizer, assessor, and program checks (`postgres.rs:2943-3340`). | Every ordinary write depends on a full value even if it touches one E/A/V. | A stable immutable tiered db-before implements the exact read contract below; assessment returns a delta overlay and compact successor state. |
| Eager kernel transition | `Database::assess_with_context` clones/applies the full current set, derives history and indexes, advances a retained treap, and runs full invariant checks (`database.rs:1455-1598`). `apply_committed_with_excision` similarly rebuilds complete state (`:1253-1440`). | Per-commit memory/work can scale with database size. | Keep these methods as semantic oracle and recovery verifier. Production assessment must use exact ranges plus a transaction-local overlay and delta validation. |
| Persisted programs | Transaction expansion, bound program hash checks, callable resolution, successor binding validation, and persisted predicates accept `&Database` (`postgres.rs:1632-2045,3072-3098,3808-3873`). `ProgramRead` already has eager/exact variants, but transaction methods still choose eager (`program.rs:746-781,999-1131`). | Hidden eager type dependency survives even after assessor extraction. | Generalize the existing exact program read path to the transaction basis/overlay. Persisted functions may read arbitrary db-before ranges; entity predicates may read arbitrary db-after ranges. |
| Process-local callbacks | `TxFunctions` callbacks and normalizer state are typed on `&Database` (`transaction.rs:524-725`). The service already rejects process-local callbacks. | Public speculative `with` and production concerns are conflated. | Retain eager callbacks as an oracle/local API adapter. The authoritative service uses persisted programs over the exact access contract; do not invent distributed Rust callback support. |
| State commitment | Each eager `Database` owns `SemanticStateCommitment`, an in-memory persistent treap (`state_commitment.rs:193-277`). `checkpoint_state_hash` is cheap only because that whole treap is retained (`:522-530`); initial checkpoint information scans current/history (`:441-475`). | Removing fact indexes alone still leaves state proportional to current database size. | Store a versioned content-addressed commitment root/nodes in PostgreSQL and update only changed paths from authenticated proofs before head CAS. |
| Commit receipt | `CommitReceipt` owns eager db-before and db-after (`postgres.rs:2060-2068`). Idempotent replay reconstructs both by replay (`:2677-2802,3006-3049`). | A transient result pins two full database values; ambiguous retries can do two full recoveries. | Return two immutable tiered handles sharing roots/cache and differing only in coordinates/tail/overlay. Resolve retries from committed coordinates without genesis replay. |
| Service report queue | `ServiceTransactionReport` owns those two databases and `Shared::publish` clones a report per subscriber (`service.rs:176-197,510-562`). | Slow or abandoned subscribers multiply full-database retention. The documented report queue is allowed to accumulate, so this is not merely transient. | Preserve the documented db-before/db-after/tx-data/tempids shape with immutable `DatabaseValue`-like handles. Every handle pins its root/generation; pin counts and queue pressure must be observable. |
| Peer compatibility | Native `PeerState` is tiered, but contains a `OnceLock<Arc<Database>>`; `db`, eager `sync`, and oracle methods can materialize it (`peer.rs:2110-2134,2769-2900,3214-3278,3776-3785,4124-4149`). | A convenient peer wrapper is unsafe as the writer representation because it carries compatibility materialization and live peer coordination. | Factor the tiered immutable value beneath `PeerSnapshot`; do not store a `Peer`/`PeerSnapshot` wholesale in the writer. Oracle materialization stays explicit and absent from production call paths. |
| Migration/backfill | Commitment migration replays each database eagerly (`postgres.rs:720-800`). | Potentially unbounded, but it is a one-time explicit administrative operation. | It may remain an offline compatibility rebuild if clearly isolated from startup and writes. It is not an acceptable fallback for activation. |
| Inspection/backup | Deep inspection and backup verification replay to `Database`; public backup verification/restore results expose it (`operations.rs:593-609`; `backup.rs:72-75,779-980`). | Broad administrative work remains eager. | Classify and document as explicit offline/deep verification, or convert result types later. It must never be called by ordinary writer activation/commit. |
| Excision/COW generation | Excision discovers requests by eager recovery; `GenerationRewriter` retains a replayed eager database while rewriting every source row (`operations.rs:1119-1165,1980-2001`; `cow_generation.rs:68-140,281-347`). | Lifecycle work can scale with all history/current state, and activation can hand that representation back toward production. | Excision is inherently broad, but activation of its completed generation must install a tiered root+tail. Whether the offline builder remains eager is an explicit Goal 15/16 deviation, not evidence of bounded ordinary operation. |

`Database` itself owns the complete representations at
`src/database.rs:222-242`; `from_genesis` and `from_index_base` build them at
`:303-499`, and `validate_invariants` rescans/rebuilds them at `:877-1029`.
These are the oracle's intended costs, not production access primitives.

## Exact production transaction read set

The interface must support all of these reads. Restricting it to the current
assessor's obvious point lookups would silently weaken transaction functions
and predicates.

| Operation | Required stable read | Current eager site | Required production rule |
|---|---|---|---|
| Coordinates | `basis_t`, entity issuance frontier, last tx instant, head/lineage/generation, durable base, state root | `database.rs:397-430`; `postgres.rs:3010-3118` | Scalar metadata; no index read. |
| Schema / idents | Resident schema, ident-to-entid, entid-to-current-name, historical aliases | `database.rs:1455-1525`; `transaction.rs:677-725` | Apply only schema-information current changes and chronological `:db/ident` assertions to the overlay. `MetadataProjection::apply` is the existing model (`peer.rs:291-375`). |
| Tempids / upsert | AVET exact `(unique-a, value)` for each unique identity, plus frontier | `database.rs:2013-2114` | Coalesce/deduplicate lookups before I/O; union identities exactly as eager oracle. |
| Lookup refs | AVET exact `(unique-a, value)` against db-before | `database.rs:2431-2464` | Must resolve only from db-before, including nested values. |
| Explicit ids | Frontier and nested tuple/ref validation | `database.rs:2466-2522` | Scalar frontier plus schema-guided recursive value check. |
| Retract without value | Current EAVT `(e,a)` | `database.rs:2147-2188` | Expand all matching db-before values; do not read a partially applied overlay. |
| CAS | Current EAVT `(e,a)` | `database.rs:2190-2238` | Compare against db-before cardinality-one value, then emit logical data. |
| `retractEntity` | EAVT entity range, recursively owned component refs, and VAET incoming ref range | `database.rs:2374-2428` | Cursor/range reads with cycle detection and measured output; no whole-index collection. |
| Entity maps/specs | EAVT entity range, resident idents/schema, specified predicate definitions | `database.rs:2261-2314` | Expand against stable db-before. |
| Schema preparation | EAVT ranges for changed schema entities and exact prior schema facts | `database.rs:1690-1705,1826-1979` | Derive successor schema from metadata delta; do not scan all current facts for each changed entity. |
| Cardinality-one tombstones | Current EAVT `(e,a)` for touched cardinality-one pairs | `database.rs:2721-2744` | Add implicit retractions after complete input expansion, matching oracle ordering semantics. |
| Composite tuples | Each touched entity's constituent `(e,a)` values and prior composite `(e,a)` | `database.rs:2747-2801` | Recompute only affected entity/tuple attributes. Missing constituents mean no derived tuple; adding a composite schema attribute does not backfill every entity. |
| Redundancy | Exact current membership of each proposed E/A/V | `database.rs:3043-3068` | Compare base plus transaction-local logical delta. |
| Cardinality / uniqueness | Touched `(e,a)` successor values; touched unique `(a,v)` owners | `database.rs:2830-2867` currently scans all successor facts | A previously valid base permits delta-local validation. Adding/changing a constraint remains the broad attribute-range exception below. |
| Schema alteration | All current facts for the altered attribute when enabling cardinality-one or uniqueness/AVET over extant data | `database.rs:1032-1160` | Use bounded-stream AEVT/AVET attribute ranges. Enabling AVET with extant values must trigger/await a covering publication or use a correct AEVT fallback. |
| Attribute predicates | Asserted value plus the predicate binding installed in db-before | Docs schema reference `:db.attr/preds`; recovered `db.clj:7794-7854`; `postgres.rs:3808-3843` | Invoke the value predicate selected from db-before. Do not grant a database argument or let a predicate installed by this same transaction govern its assertions. |
| Entity predicates | Arbitrary exact reads from the complete db-after | `database.rs:2976-3029`; `postgres.rs:3845-3873` | Overlay must expose current and history as base merged with the entire assessed transaction. |
| Persisted tx functions | Arbitrary exact reads from db-before; nested emitted forms repeat normalization/expansion | `postgres.rs:1772-1870`; `program.rs:1131-1225` | One locked basis throughout recursion, with program work/recursion/output limits preserved. |
| Commitment | For each changed current E/A/V, prior membership plus authenticated commitment path | `state_commitment.rs:210-277` | Verify proof, apply insert/delete path-copy, persist nodes, obtain successor root. No complete semantic treap. |
| History/time views | Arbitrary ordered history range for programs/predicates and immutable report values | Docs transaction model; `DatabaseValue` and `PeerSnapshot` | History overlay is base history plus all new transaction datoms; time views retain basis/root/generation pins. |

The current full calls to `apply_logical`, `validate_cardinality`,
`validate_uniqueness`, index reconstruction, and `validate_invariants`
(`database.rs:1455-1598,2804-2867,877-1029`) must not be copied into the
production implementation. Differential tests against them establish that
delta-local work preserves the complete information-set semantics.

## Recovered representation and lifecycle map

| Recovered element | Role | Closest current Rust piece | Required disposition |
|---|---|---|---|
| `Db.memidx` | Persistent in-memory indexes for the active log tail | `RecentTier` (`recent.rs:334-890`) | Reuse/refactor beneath a writer-owned tiered value. Track datoms and accounted bytes. |
| `Db.indexing` + `indexingNextT` | Frozen prefix handed to indexer while a new active memidx accepts writes | No equivalent writer state; indexer independently reads SQL tail | Add an explicit immutable frozen tier/frontier or an equivalent publication token. Do not lose post-freeze transactions. |
| `Db.mid-index` | Previously produced intermediate tier | PostgreSQL native persistent tree staging/publication | It may be omitted if the persistent-tree algorithm has no semantic need for a second durable tier; record that concrete PostgreSQL deviation. |
| `Db.index` / `history` | Durable current/history tree roots | `TreeBase` + `PostgresTreeStore` (`peer.rs:275-284`) | Factor out of peer ownership and share with writer values. |
| `Db.memlog` | Recent tx data used for handoff/accounting | Authenticated log tail + `RecentTier` | Keep coordinates and bounded recent transaction data; PostgreSQL log remains authority. |
| `elements`, ident keys/ids | Resident schema and idents | `MetadataProjection` (`peer.rs:285-375`) | Reuse exact incremental projection, preserving ident assertion history. |
| `basisT`, `nextT`, `indexBasisT` | Logical endpoint, allocation endpoint, durable root endpoint | Peer basis/frontier/durable base; SQL head | Make one immutable coordinate struct and validate all four roots against it. |
| `acceptDataCheck` / `addData` | Incrementally add assessed datoms to memidx and run hooks | `RecentTier::apply` plus eager `Database` transition | Production successor appends only tx datoms/delta metadata. Eager path remains oracle. |
| `seek*` | Lazy ordered merge of memidx, indexing, mid-index, durable root, and conditional history | `PeerIndexCursor`, persistent-tree cursor, recent cursor (`peer.rs:3468-3945`) | Extract a common cursor merge. Prefix reads must seek and stop, not collect an unbounded `Vec`. |
| `prepare-for-indexing` | Atomically freeze memidx and reset active memidx | Indexer consolidation lacks writer handoff | Capture endpoint tuple and frozen tier while commits continue into a new active tier. |
| `complete-indexing` / accept index | Adopt only a result matching the captured frontier; preserve newer memidx tail | Peer refresh/publication checks | Require `(generation,basis,tx_hash,state_hash)` equality, adopt root, and drop only the covered frozen/tail prefix. |
| `ProcessInpoint`, `filter-assess-tx-datoms`, `with-tx` | Expand against db-before, assess complete data, construct db-after, then ensure predicates | Rust normalizer/assessor/program runtime | Preserve control flow exactly through the access seam and overlay. |
| `log/catchup` | Seek at `nextT`, authenticate/reduce only tail, account bytes | `read_authenticated_log_range` + `RecentTier::apply` | Make this normal activation and failover path. |
| `IndexerImpl` accounting | Per-db memidx/indexing accounting, threshold scheduling, hard process limit | Service backlog and peer recent stats are separate | Establish one writer-state source of truth; backpressure on actual active+frozen recent bytes. |

The recovered source locations are `db.clj:4719-4975,5035-5074,5599-5783,
6525-6614,7451-7925`, `update.clj:691-875,1085-1299,2793-3005`,
`log.clj:1406-1520`, and `indexer.clj:262-329`.

## Minimal Rust seam

Keep this interface crate-private and PostgreSQL-concrete. A sealed enum with
methods is preferable to a public generic storage trait.

```rust
struct BasisCoordinates {
    basis_t: u64,
    eidx_frontier: u64,
    last_tx_instant: Option<i64>,
    generation: u64,
    durable_base_t: u64,
    tx_hash: Digest,
    state_hash: Digest,
}

enum TxBasis<'a> {
    Eager(&'a Database),
    Tiered(&'a TieredDatabase),
}

trait ExactIndexAccess {
    fn coordinates(&self) -> BasisCoordinates;
    fn schema(&self) -> &Schema;
    fn entid(&self, ident: &Keyword) -> Option<u64>;
    fn ident(&self, entity: u64) -> Option<&Keyword>;
    fn range(&self, view: View, range: IndexRange)
        -> Result<MeasuredCursor<'_>, SemanticError>;
}

struct SuccessorOverlay<'a> {
    base: TxBasis<'a>,
    // Immutable ordered add/retract sets for each required index.
    delta: TransactionDelta,
    metadata: MetadataProjection,
    coordinates: BasisCoordinates,
}
```

`prefix` is a convenience that compiles to a left-contiguous range. The
primitive is a fallible measured cursor because persisted programs may perform
arbitrary ranges and `retractEntity` can be large. Every cursor reports at
least durable nodes/bytes read, recent datoms examined, ranges opened, and
datoms yielded. There is deliberately no `materialize()` method.

`TieredDatabase` should contain coordinates, an `Arc` durable `TreeBase`, an
active `RecentTier`, optional frozen indexing tier, resident
`MetadataProjection`, a durable semantic commitment root, bounded cache, and
root/generation pins. It must not contain `PeerCore`, peer update/report
machinery, a compatibility `OnceLock<Database>`, or a generic storage backend.

Assessment should return an immutable compact result such as
`AssessedTransaction { tx_data, tempids, successor_overlay, metadata_delta,
commitment_update }`. Publication persists content-addressed commitment/tree
objects first, performs the existing expected-head/epoch CAS, and installs the
compact successor only after PostgreSQL commit acknowledgement. A failed CAS
or ambiguous result cannot mutate the installed writer value.

## Required broad-operation and deviation ledger

1. **PostgreSQL instead of Datomic storage/log pods.** Preserve immutable
   content, expected-root publication, and root+tail recovery; do not copy
   storage-specific pod/CAS machinery from `log.clj`.
2. **No `mid-index` unless proven necessary.** The native persistent-tree
   publisher can replace this physical tier, but the active/frozen recent
   handoff and matching-frontier rule are semantic performance requirements.
3. **Commitment treap is Atomic-specific.** 1.0.7705 is evidence for tiered
   indexes, not this repository's state-hash representation. Preserve the
   existing versioned digest exactly. Because state hash participates in
   generation transaction hashes, changing it requires an explicit generation
   migration boundary; history cannot be silently rewritten.
4. **AVET readiness is a physical publication boundary.** Recovered Datomic detects a schema datom
   requiring AVET and forces an index job (`db.clj:3457-3606`). Native peers
   mark such attributes `avet_unready`: qualified raw AVET seeks reject the
   affected attribute, unqualified raw AVET scans omit it, and logical queries
   use AEVT until a covering root is adopted (`peer.rs:362-375,3909-3939`). The
   writer forces/schedules that bounded attribute job and correctly scans AEVT
   until the covering root is adopted. Tests prove the query planner changes
   from AEVT to AVET without changing the answer; an availability error is
   confined to qualified raw AVET access while the physical index is pending.
5. **Administrative full scans are explicit exceptions.** Initial native-root
   construction, deep verification, export/import, and privacy excision may
   stream the full history. They must be isolated, measured, and never hidden
   behind ordinary activation, retry, transaction, or report APIs.
6. **Resident metadata is a qualified bound.** Idents and schema are required
   to remain resident and may grow with their own cardinality. The promised
   bound is independence from total ordinary current/history facts, subject to
   configured active/frozen recent tiers, cache, one transaction/result, and
   explicit metadata size reporting.
7. **Reports pin immutable values.** Queueing handles rather than eager values
   fixes database-sized duplication, but old roots/generations remain live
   while clients retain reports. Pin count/age and GC interaction are part of
   the observable contract.
8. **Initial root absence is not normal failover.** A one-time administrative
   build is acceptable. Thereafter activation must use a covering root+tail or
   fail/repair explicitly; silently replaying genesis is a regression.
9. **Retained history is a projection, not a second authority.** Ordinary
   incremental jobs filter only ranges they rebuild and only under endpoint
   `noHistory`; explicit reconstruction may choose another admissible subset
   from the immutable log. Goal 20 owns semantic deep checks for backup and
   request-base archives, plus disclosure of every retained-content anchor.

## Completed proof obligations

1. Added `src/program.rs`, `src/transaction.rs`, service report queues,
   idempotent outcome reconstruction, and generation lifecycle paths to the
   formal eager-dependency audit; the repair was not limited to the old writer
   cache.
2. Generalized the exact persisted-program read path before switching the
   assessor, so functions and predicates do not force eager materialization.
3. Added transaction-local coalescing/memoization with exact I/O stats.
   Repeated semantic reads no longer repeat cursor/SQL work.
4. Made schema constraint changes and AVET backfill explicit broad streamed
   operations; ordinary validation is delta-local against a valid base.
5. Built the durable incremental commitment before switching production
   assessment; ordinary publication never materializes the complete set.
6. Factored tree base, metadata, cursors, cache, and pins beneath both peer and
   writer without putting live peer coordination in writer state.
7. Made indexing handoff an exact endpoint publication: new transactions stay
   in the newer tail, only the captured endpoint is adopted, and backlog
   metrics follow actual recent/publication work.
8. Changed service receipts/reports and idempotent replay to tiered values sharing
   roots; retained reports pin explicit immutable generations rather than full
   eager databases.
9. Tested activation, reconnect, cache miss, retry outcome, standby takeover,
   generation replacement, and no-native-root behavior with a counter that
   proves `recover_to`, compatibility materialization, and eager-value
   residency remain zero on normal paths.
10. Measured representation-level counts rather than RSS: eager database
    values/current/history/index/semantic nodes, active and frozen recent
    datoms/bytes, cache entries/bytes, cursor node reads/bytes, commitment
    proof nodes, and pinned roots/generations.
11. Made sealed tree-build intents exact liveness owners for retirement. The
    Rust candidate selector and migration-22 privileged collector take both
    fences and refuse to retire a matching publication; the upgrade also
    drains the state-2 orphan form that an older collector could strand.

`tests/transactor_residency.rs` is now the permanent inverse of the historical
baseline. After 128 commits it requires zero eager writer values/facts/history,
an empty recent tier after consolidation, cache residency within configured
bounds, cold root-only activation, and localized read/commitment work below
the total history size. `tests/transactor_differential.rs` drives the real
service through generated accepted and rejected transitions and compares the
complete outcome against `Database::with`; restart, failover, ambiguity,
backpressure, physical AVET readiness, corruption, and repeated schema-event
witnesses cover the remaining risky boundaries. Deep inspection now compares
every usable retained native publication with its named authoritative log
value and proves its sibling indexes derive from current EAVT plus an
admissible `noHistory` history projection. A live tree witness checks identical
retained fact sets across EAVT/AEVT/AVET/VAET despite different leaf layouts.
These tests count as evidence only when the PostgreSQL environment is
explicitly present and the work actually executes; Goal 20 owns a non-skipping
harness for the integrated suite. That gate must extend semantic equivalence
to portable backup and request-base archive trees and report
backup/archive/retired-root/snapshot/WAL retention anchors. Those remaining
obligations are assigned, not claimed complete here.
