# Goal 16 source map and transaction access contract

This is a dependency map, not evidence that Goal 16 is complete. The current
production transactor still retains and reconstructs eager `Database` values.
The Stage 1 residency counter and long-history witness make that failure
measurable; they do not repair it.

## Semantic constraints

- A database is an immutable value and a transaction is a declarative,
  unordered, atomic function from db-before to db-after
  (`datomic_pro_docs/04_transactions/01_transaction_model.md:13-48`).
- Transaction functions see db-before; entity predicates see the complete
  db-after. Neither may observe an intermediate transaction state
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

## Where eager `Database` exists in production

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
| Attribute predicates | Arbitrary exact reads from db-before | `postgres.rs:3808-3843`; `program.rs:999-1125` | Persisted runtime uses the same range interface; opaque programs justify prefetch hints/budgets, not semantic restriction. |
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
4. **AVET readiness differs today.** Recovered Datomic detects a schema datom
   requiring AVET and forces an index job (`db.clj:3457-3606`). Native peers
   mark such attributes `avet_unready` and reject AVET reads until a covering
   root (`peer.rs:362-375,3909-3939`). The writer must force/await publication
   or correctly scan AEVT for the affected attribute; an availability error
   cannot become the permanent semantics of new writes.
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

## Stage-plan corrections and proof obligations

1. Add `src/program.rs`, `src/transaction.rs`, service report queues,
   idempotent outcome reconstruction, and generation lifecycle paths to the
   formal eager-dependency audit. Replacing only `PostgresStore.current` is
   insufficient.
2. Extract/generalize the exact persisted-program read path before or with the
   assessor. Otherwise transaction functions and predicates force eager
   materialization back in.
3. Add a transaction read-planning/prefetch phase with coalesced ranges and
   I/O stats. One PostgreSQL/tree lookup per datom is semantically correct but
   violates the recovered performance design and transaction-hints evidence.
4. Make schema constraint changes and AVET backfill explicit broad streamed
   operations. Ordinary validation must be delta-local against a previously
   valid base.
5. Build the durable incremental commitment before switching production
   assessment. A lazy fact overlay followed by full commitment
   materialization is not a bounded writer.
6. Factor `TreeBase`, metadata, cursors, cache, and pins beneath both peer and
   writer. Do not make `PeerSnapshot` the writer state.
7. Model indexing handoff explicitly: freeze recent, open a new active tier,
   publish for the captured endpoint, adopt only an exact matching result, and
   retain the newer tail. Unify service backlog metrics with actual writer
   recent/indexing bytes.
8. Change receipts/reports and idempotent replay to tiered values sharing
   roots. Test slow subscribers and retained old reports, not only immediate
   transaction responses.
9. Test activation, reconnect, cache miss, retry outcome, standby takeover,
   generation replacement, and no-native-root behavior with a counter that
   proves `recover_to`, compatibility materialization, and eager-value
   residency remain zero on normal paths.
10. Measure representation-level counts rather than RSS: eager database
    values/current/history/index/semantic nodes, active and frozen recent
    datoms/bytes, cache entries/bytes, cursor node reads/bytes, commitment
    proof nodes, and pinned roots/generations.

Stage 1 has a measured baseline in
`tests/transactor_residency.rs`: after 64 commits and cold eager recovery it
asserts one eager writer value and history/current counts that grow with the
database. The PostgreSQL witness is configuration-gated and is evidence only
when `ATOMIC_POSTGRES_URL` is set and the test actually runs. Stage 1 remains
complete because the exact access seam now has eager and native executable
implementations and both PostgreSQL witnesses actually ran on PostgreSQL
15.11. The eager-count assertion is a historical baseline and will be inverted
to the permanent zero-eager regression gate when the production writer swaps
representations in Stage 4.
