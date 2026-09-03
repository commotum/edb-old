# Corrective Evidence Ledger

This ledger records the 2026-09-03 cross-check of the corrective audit. Paths
are repository-relative. `datomic_pro_docs` governs observable semantics;
`1.0.7705` supplies the default implementation witness. PostgreSQL/Rust safety
requirements are labeled rather than misrepresented as Datomic behavior.

## Confirmed semantic and architectural corrections

### C01 — Separate `t`, transaction entities, system entities, and user entities

- **Docs:** `02_core_concepts/05_glossary.md:302-312` distinguishes `t` from a
  transaction entity; `04_transactions/07_partitions.md:12-14,32-42` encodes
  system, transaction, and user partitions in entity-ID high bits.
- **1.0.7705:** `datomic.db/make-eid`, `eid->part`, and `eid->eidx` are in
  `peer/src-clj/datomic/db.clj:350-376`; `datomic.peer/t->tx` calls
  `(make-eid 3 t)` at `peer.clj:2144`; `BOOT-IDS` identifies tx partition 3 and
  user partition 4 at `db.clj:121-140`.
- **Repair:** use disjoint encoded/typed identity and checked arithmetic. Full
  application-controlled partition support remains optional.

### C02 — Allocate permanent IDs only behind an issued frontier

- **Docs:** `03_schema/03_identity_and_uniqueness.md:26-30` assigns IDs at the
  transactor; `04_transactions/02_transaction_data.md:248-274` uses tempids for
  new entities.
- **1.0.7705:** `datomic.db/get-ids`, `db.clj:6640-6817`, allocates/upserts and
  rejects an explicit non-temp ID at or beyond `nextT` at `:6798-6804`.
- **Repair:** reject caller-minted future IDs while allowing an issued entity
  whose current facts were later retracted. Do not impose the false rule that
  every explicit ID must currently have a datom.

### C03 — Make schema and general idents authoritative datoms

- **Docs:** `03_schema/00_schema_data_reference.md:58-68,152-179` says schema is
  ordinary data and `:db/ident` names entities; `03_schema/02_data_modeling.md:9-35`
  uses idents for enum entities; `04_transactions/02_transaction_data.md:286-310`
  permits idents anywhere an entity is expected.
- **1.0.7705:** `datomic.db/bootstrap-data`, `db.clj:7983-8055`, is ordinary
  E/A/V schema data; `key-hook`, `db.clj:2550-2568`, derives the ident map;
  `install-attribute-hook`, `db.clj:3123-3196`, derives `Attribute` objects from
  the proposed database.
- **Repair:** retain typed Rust schema/ident maps only as rebuildable indexes.
  Preserve rename aliases. Operative schema does not automatically rewind for
  `as-of`: docs `03_schema/01_changing_schema.md:11-15` and source
  `Db.asOf`/`since`, `db.clj:5139-5140`, reuse current-basis schema.
- **Exact hook history:** recovered `attrs-missing-hooks`,
  `db.clj:4290-4462`, synthesizes hooks only after redundant property datoms
  are filtered, while `db.clj:4854-4864` preserves repeated A=19 assertions as
  real alteration events. The repair therefore emits no hook for no-op
  metadata, ident-only rename, or custom facts, but requires exactly one
  install/alter event for every material hook-controlled schema target and
  rejects missing, wrong, or orphan events during recovery.
- **Observed implementation evidence:** Goal 11 persists exact supported
  genesis at t=0 and application schema as ordinary t=1 data; derives schema
  and general idents from current/history information; removes the durable
  schema blob/change channel; and proves direct eid/ident/lookup-ref reads,
  alias repurpose, current-basis temporal interpretation, PostgreSQL restart,
  backup/restore, and coherent derived-base fallback. See
  `goal-11/0-plan.md` for the non-skipping gate.

### C04 — Validate a transaction's final facts under its successor schema

- **Docs:** `04_transactions/01_transaction_model.md:30-40` requires complete,
  unordered valid database values; `03_schema/01_changing_schema.md:9-15,34-47`
  requires invalid synchronous schema changes to abort.
- **1.0.7705:** `ProcessInpoint`, `db.clj:6525-6607`, resolves ordinary data
  against db-before; `filter-assess-tx-datoms`, `:4430-4486`, assesses the full
  information set; `Db.addData`, `:4807-4965`, invokes alter/install hooks on
  the proposed after value; `card-many->card-one`, `:3265-3271`, scans it.
- **Repair:** retain db-before attribute resolution, but validate final facts
  against the successor schema before returning or publishing.

### C05 — Enforce recovered tuple installation invariants

- **Docs:** `03_schema/00_schema_data_reference.md:302-322,396,455-465` defines
  legal scalar slots, generated composites, and discontinuation; tuple schema
  is not alterable per `03_schema/01_changing_schema.md:125`.
- **1.0.7705:** `tuple-value-types`, `db.clj:2693-2709`;
  `tuple-attr-value-type`, `:2729-2738`; and `tuple-install-errors`,
  `:2773-2857`, require legal scalar/cardinality-one constituents, valid shape,
  and no discontinuation at install.
- **Repair:** close the Rust validator gaps, including `Float`, `Bytes`, nested
  `Tuple`, composite cardinality, constituent existence/type, and immutable spec.

### C06 — Remove Rust `Debug` output from semantic normalization

- **Docs:** transactions are unordered complete information sets at
  `04_transactions/01_transaction_model.md:32-48` and
  `04_transactions/02_transaction_data.md:57-65`.
- **1.0.7705:** typed structural keys `AVof`, `EAVof`, and `EAOpof` implement
  conflict/dedup at `db.clj:4181-4245`; persistent ordering uses `eavt-cmp` at
  `:885-907`; it does not sort globally by printed forms.
- **Repair:** use structural sets/comparators. A versioned canonical native sort
  is allowed for reproducibility but is not itself a Datomic semantic promise.

### C07 — Preserve BigDecimal stored-scale distinctions in persistent indexes

- **Docs:** `03_schema/00_schema_data_reference.md:297-300` treats scale as
  significant enough to require consistent comparison scale.
- **1.0.7705:** `datomic.common/equals-with-strict-scale`,
  `common.clj:157-172`, and its database uses at `db.clj:4856,7303` distinguish
  equal magnitudes with different scale at stored-equality boundaries.
- **Repair:** persistent ordering/loading needs a canonical stored-value
  tie-breaker instead of rejecting or conflating legal scale variants.

### C08 — Implement persistent tiered indexes and lazy peers

- **Docs:** `06_indexes/01_index_model.md:83-101` and
  `06_indexes/02_background_indexing.md:15-17` require shallow immutable trees,
  a recent memory tier, cacheable segments, and sublinear affected-range merges.
- **1.0.7705:** `datomic.index.RootNode`/`DirNode`, `index.clj:233-319`;
  lazy `Index.seek`, `:1349-1410`; incremental `Db.acceptDataCheck`,
  `db.clj:4759-4805`; multi-tier `Db.seek*`, `:5035-5074`; affected-range
  `merge-one-index`, `index.clj:3439-3713`; and `merge-db`, `:6485-6494`.
- **Repair:** full arrays remain an oracle only. Durable roots, seeks,
  consolidation, cache residency, and recovery must be incremental and lazy.

### C09 — Use a shared, atomically advancing peer connection

- **Docs:** `04_transactions/06_client_synchronization.md:12-26` describes a
  connection advancing through immutable values; lazy local entities are at
  `02_core_concepts/03_entities.md:83`.
- **1.0.7705:** thread-safe cached connections are described at
  `datomic.peer`, `peer.clj:2-6`; `connection-cache`/`get-connection` are at
  `:251-254,1579-1631`; `Connection.db` dereferences shared `db_ref` at `:792-793`.
- **Repair:** expose a cloneable Rust handle over shared advancing state while
  preserving immutable captured database values.

### C10 — Propagate the exact database value through all query work

- **Docs:** a db is an immutable point-in-time value at
  `02_core_concepts/00_datomic_data_model.md:16`; filters remain derived
  database values at `02_core_concepts/02_database_filters.md:9`; query
  functions accept explicit sources at `05_query_and_pull/02_query_reference.md:903,927,960`.
- **1.0.7705:** `Db` retains temporal/filter state at `db.clj:4719-4744` and
  `windowed` applies it at `:1810-1831`; `missing?`, `get-else`, and `get-some`
  use their supplied db in `extensions.clj:131-203`; `query/pull-fv` passes the
  selected source in `query.clj:1354-1394`.
- **Repair:** patterns, expressions, extensions, aggregates, and pull must
  observe one exact source view.

### C11 — Correct already-claimed query primitives

- **`get-some`:** docs `query_reference.md:924-943` and source
  `extensions.clj:143-203` require cardinality-one and return
  `[attribute-id,value]`.
- **`ground`:** docs `:945-954`; source `extensions.clj:204-215` and
  `datalog.clj:2266-2309` require one constant.
- **tuple nil:** docs `03_schema/00_schema_data_reference.md:322,425-431` and
  source `extensions.clj:331-337`/`db.clj:6282` preserve nil; never synthesize
  an empty string.
- **min/max n:** docs `query_reference.md:337-349,428-441,1280` and source
  `aggregation.clj:28-59` retain bag duplicates.
- **rules:** docs `query_reference.md:1379-1496` require invocation bindings;
  source `datalog/sched-in-order`, `:1766-1895`, `eval-rule`, `:2974-3041`, and
  `qsqr`, `:3245-3265`, converge to an actual fixed point without a 128-round
  semantic ceiling.
- **ident aliases/entity inputs:** docs
  `04_transactions/02_transaction_data.md:298-310` and
  `05_query_and_pull/03_pull.md:10-12`; source `db/resolve-id`,
  `db.clj:1197-1228`, is used by pull and database relations.

### C12 — Keep expected-head CAS internal to ordinary transaction submission

- **Docs:** the transactor queues declarative transactions and processes them
  serially at `04_transactions/03_processing_transactions.md:9-17`; transaction
  functions exist because they see the actual db-before at
  `04_transactions/01_transaction_model.md:32-48`.
- **1.0.7705:** `transaction/create-procargs`,
  `transactor/src-clj/datomic/transaction.clj:353-355`, sends id/data/options
  without a basis; `update/process-transaction`, `update.clj:1100-1314`, applies
  against current `db-ref`; internal revision CAS exists in
  `cluster.clj:155-170` and `log.clj:547-583`.
- **Repair:** normal public transact has no required basis. An optional explicit
  compare-basis extension may remain.

### C13 — Assign and bound transaction time at the transactor

- **Docs:** `04_transactions/02_transaction_data.md:355-378` says the
  transactor supplies `:db/txInstant`; an override cannot precede the basis or
  exceed its clock.
- **1.0.7705:** `db/has-tx-inst?`, `db.clj:6831-6870`, enforces current tx,
  past/future, and multiplicity; `next-valid-inst`, `:6882-6890`, and processing
  at `:7451-7489` capture/insert time.
- **Repair:** keep the pure kernel clock-explicit; authoritative submission uses
  server time by default with a separately bounded import override.

### C14 — Integrate sufficient db-before functions and db-after predicates

- **Docs:** `04_transactions/04_transaction_functions.md:15-45,105-159,212-233`
  requires functions that query db-before and emit transaction data;
  `04_transactions/01_transaction_model.md:44-75` and
  `04_transactions/05_acid.md:31` require entity validation over complete db-after.
- **1.0.7705:** recursive emitted forms are injected at `db.clj:7534-7550`;
  entity predicates run at `:7703-7769`; the final db/report pipeline is
  `:7867-7925`; function deployment/execution is in `function.clj:56-249`.
- **Repair:** keep the no-ambient-authority sandbox, but add sufficient
  branching/iteration/query/data forms and run it through the fenced service.
  Preserve `d/cancel` as typed bounded anomaly data rather than flattening its
  qualified serializable values to diagnostic strings
  (`04_transaction_functions.md:162-173`).

### C15 — Preserve acknowledgement semantics and one database-bound writer

- **Docs:** transaction completion follows storage acknowledgement and timeout
  may be unknowable at `04_transactions/05_acid.md:45-47` and
  `04_transactions/03_processing_transactions.md:19-21`; reconnect guidance
  warns against blind retry at `08_operations/00_architecture_and_storage/02_datomic_deployment.md:53-61`.
- **1.0.7705:** `update.clj:1656-1717,1784-1839` resolves logged promises and
  responds only after durable append; active ownership/catch-up is established
  in `lifecycle.clj:36-208` and `update.clj:2793-2813`.
- **Repair:** no ordinary error may follow an acknowledged commit; all writes
  must use the one service and every fence must be bound to the served database.
  PostgreSQL epoch mechanics are a native design.

### C16 — Recover transactor/peers from durable base plus log tail

- **Docs:** log is authoritative while indexes are derived at
  `00_start_here/00_introduction.md:79-109`; recent memory index rebuild starts
  after the durable tier at `06_indexes/02_background_indexing.md:13-19`.
- **1.0.7705:** startup loads a root then catches up in
  `update.clj:2793-2857`; `log.clj:1406-1520` seeks/reduces only the tail.
- **Repair:** remove per-commit and startup genesis replay once a verified base
  exists; carry db-before in the original assessed report.

### C17 — Retire superseded physical roots after a safe boundary

- **Docs:** `08_operations/01_capacity_and_reliability/00_capacity_planning.md:275-286`
  describes garbage generated by indexing and delayed safe collection.
- **1.0.7705:** `index.clj:3940-3985,6327,6433-6448` and
  `update.clj:1617-1655` mark old roots/dirs after publication;
  `garbage.clj:318-390,475-603` deletes after a boundary.
- **Repair:** historical logical information does not make every obsolete
  physical manifest immortal; use grace/pins before deletion.

### C18 — Correct excision selection, audit, and synchronization semantics

- **Docs:** `09_optional/02_specialized_operations/02_excision.md:10-15,33-58,103-129`
  covers privacy scope, entity 42, protected data, recursive components/inbound
  refs, permanent predicate record, background rewrite, and synchronization.
- **1.0.7705:** `excise/keeper?`, `excise.clj:139-147`, protects partition
  zero/exact boot IDs rather than a numeric range; `pred-and-extent`,
  `:174-266`, implements cutoff/component/ref closure; log/tree adoption is in
  `update.clj:2090-2117`.
- **Repair:** remove the `<1000` heuristic, preserve the queryable/auditable
  predicate, and state precisely what caches/backups/WAL remain. Excision is
  requested as ordinary database information and runs asynchronously; backup
  is strongly recommended operational preparation, not a semantic prerequisite.
  Atomic SQL/generation publication may strengthen crash atomicity, but may not
  turn that recommendation into a mandatory gate.

### C19 — Separate migrations from runtime and allow secure connections

- **Docs:** SQL provisioning is explicit at
  `08_operations/00_architecture_and_storage/00_storage_services.md:107-131`;
  intentional version-ordered logical base-schema upgrades are explicit/not
  every connection at
  `08_operations/00_architecture_and_storage/02_datomic_deployment.md:235-258`;
  SSL and trust configuration is documented in
  `00_architecture_and_storage/01_transactor_reference.md:17-27,81-132`.
- **1.0.7705:** `kv_sql_ext.clj:113-170` builds/validates a runtime pool and
  passes driver/TLS parameters; `sql.clj` borrows connections around CRUD/CAS
  rather than startup DDL; `artemis_server.clj:71-112` supports TLS transport.
  The deployment docs require peers to reconnect without application action,
  retain their latest consistent database value during outage, and let sync
  wait for availability (`02_datomic_deployment.md:47-61`).
- **Repair:** runtime roles do not execute DDL; every runtime constructor checks
  the installed physical schema before reading data; a dedicated migrator owns
  the native checksummed SQL protocol. Populated native schemas older than v6
  require old-decoder export/rebuild because the recovered transaction/schema
  representation changed; v6--v8 upgrades canonically replay the immutable log
  to replace migration 9's zero state-commitment placeholders before any new
  schema is committed. Runtime handles retain their concrete connection policy;
  peers and standby startup reborrow through restart without discarding an
  immutable peer value, and timed sync caps connection negotiation by its
  remaining deadline. Arbitrary writes are never automatically retried.
  Dedicated writer/peer roles reject
  inherited, owning, schema-creating, PUBLIC, and column-level ambient
  authority, clear direct grants across every actual `atomic_*` table rather
  than only the positive whitelist, and then receive an exhaustively tested
  effective ACL. PostgreSQL TLS uses configurable trust but always validates
  when required. The checksum protocol and relational grants are native
  production mechanics, and verified SQL TLS is deliberately stricter than
  the documented non-validating example; neither is misattributed to Datomic's
  logical `:upgrade-schema` operation.

### C20 — Preserve consistent, differential, root-last backup behavior

- **Docs:** live/differential backups and restore points are at
  `08_operations/01_capacity_and_reliability/02_backup_and_restore.md:18-85`;
  shallow/deep verification is at `:105-118`; backup storage is per database at
  `:32-40`.
- **1.0.7705:** `backup.clj:1177-1210` captures a coherent point;
  `:1517-1563` reuses segments; `:1572-1632` copies nodes then roots last;
  `:1348-1420` restores; `:1890-1939` verifies reachability/readability.
- **Repair:** stable database identity, exact restore state, and no arbitrary
  one-million-transaction ceiling. Temp-file rename, directory fsync, and
  opaque proof types are Atomic-specific safety. Deep verification is an
  explicit backup operation; it is not an excision authorization token.

### C21 — Make database functions temporal information, not mutable deployment state

- **Docs:** database functions are installed by asserting `:db/fn`, selected
  through their `:db/ident`, versioned in the database, and lazily compiled and
  cached at `04_transactions/04_transaction_functions.md:49-66,208-216`.
- **1.0.7705:** `Db.getFn`, `peer/src-clj/datomic/db.clj:5017-5034`, resolves
  the entity in that immutable `Db` and reads attribute 52 (`:db/fn`);
  bootstrap ids 26/52 are at `:7983-8055`; the lazy compiled target is retained
  by `datomic.function.Function` at
  `transactor/src-clj/datomic/function.clj:56-99,218-243`.
- **Repair:** content-addressed native blobs are a Rust strengthening, but the
  selected hash must be an ordinary temporal `Value::Function` datom. Mutable
  SQL activation rows may not select transaction or predicate behavior.
  Current, historical, backup, GC, retry, and cache paths must all retain that
  same meaning.

### C22 — Preserve recursive full transaction data and real db-before query capability

- **Docs:** a transaction function is pure `[db-before,args] -> tx-data`, sees
  neither sibling input nor sibling output, may return empty data or nested
  calls, and may run declarative queries at
  `04_transactions/04_transaction_functions.md:13-25,152-190` and
  `04_transactions/01_transaction_model.md:44-75`. Transaction data includes
  list operations and entity maps at `04_transactions/02_transaction_data.md:30-65,136-213`.
- **1.0.7705:** `ProcessExpander.inject` recursively feeds every returned form
  through `ProcessInpoint` against its captured db-before at
  `peer/src-clj/datomic/db.clj:6525-6612,7491-7550`; compiled functions receive
  `d/q` at `transactor/src-clj/datomic/function.clj:145-161`.
- **Repair:** the fenced service accepts one unordered transaction-form set;
  the native runtime needs structured values/control flow, full ordinary form
  expansion, and a canonical metered conjunctive-query host. Fixed EAV
  traversal alone is useful but is not evidence of Datalog.

### C23 — Resolve predicates only at their assessed validation boundary

- **Docs:** attribute predicates apply only to asserted values surviving the
  transaction and begin on the transaction after installation; entity specs
  run only when explicitly ensured at
  `03_schema/00_schema_data_reference.md:509-619`.
- **1.0.7705:** attribute descriptors retain delayed resolution in
  `peer/src-clj/datomic/db.clj:2942-3065`; invocation follows assessment at
  `:4430-4462,7782-7855`; entity predicates resolve only inside requested
  ensures at `:7703-7769`.
- **Repair:** an unused missing, corrupt, or wrong-role predicate must not
  reject an unrelated transaction. Resolve and execute only predicate symbols
  named by surviving assertions and requested ensures. Fully qualified symbols
  are required; the native persisted-symbol-to-temporal-function mapping is an
  explicit replacement for Datomic's classpath resolution.

### C24 — Bind a derived base to authoritative database information

- **Docs:** the log is authoritative while indexes are derived and disposable
  at `00_start_here/00_introduction.md:79-109`; durable and memory index tiers
  are recombined at `06_indexes/02_background_indexing.md:13-19`.
- **1.0.7705:** startup adopts a durable root and then reduces the authoritative
  log tail at `transactor/src-clj/datomic/update.clj:2793-2857` and
  `peer/src-clj/datomic/log.clj:1406-1520`.
- **Repair:** a manifest checksum and matching endpoint transaction id do not
  prove that independently supplied current/history datoms are the state
  derived from that log. Atomic must commit a canonical state/root digest at
  authoritative publication and require an adopted base to match it. The
  native digest format is PostgreSQL/Rust-specific; the authority direction is
  recovered rather than invented.

### C25 — Keep the production transactor tiered as well as the peer

- **Docs:** the memory index exists in every database process, is rebuilt from
  the log after the durable tier, and cannot grow forever
  (`06_indexes/02_background_indexing.md:15-19`). Database size may exceed
  application memory while immutable segments are fetched and cached on demand
  (`00_start_here/00_introduction.md:79-100`).
- **1.0.7705:** recovered `datomic.db.Db` stores separate `memidx`, `indexing`,
  `mid-index`, durable `index`, and `history` fields
  (`peer/src-clj/datomic/db.clj:4719-4744`); `Db.seek*` merges those tiers
  lazily (`:5035-5074`), while `prepare-for-indexing` moves rather than copies
  the memory tier (`:5599-5610`). `datomic.indexer.IndexerImpl` separately
  accounts `:memidx` and `:indexing` bytes and applies the hard limit
  (`transactor/src-clj/datomic/indexer.clj:262-329`).
- **Observed native gap:** `PostgresStore.current` retains a complete eager
  `Database` per served database (`src/postgres.rs`), and that oracle owns full
  current/history collections. Native background publication can bound its
  separate novelty counter without bounding this retained state.
- **Repair:** after Goal 14 establishes one exact lazy `IndexAccess` seam for
  query/pull, use the same semantic access boundary for transaction expansion,
  validation, predicates, state commitment, and reports. Keep the eager
  `Database` only for pure/reference tests; production activation and commits
  must retain durable roots plus bounded memory/indexing tiers and load only
  the ranges actually needed. This is Goal 16; it is not waived by Goal 13's
  peer-cache evidence.

### C26 — Keep schema and ident projections out of physical root manifests

- **Docs:** schema and idents are database information
  (`03_schema/00_schema_data_reference.md:58-68,152-179`), while durable index
  roots and cacheable segments are derived from the authoritative log
  (`00_start_here/00_introduction.md:79-109`). Idents are expected resident
  metadata, not a second durable authority (`03_schema/00_schema_data_reference.md:158-160`).
- **1.0.7705:** a durable index root contains basis/revision/schema level and
  physical index root ids, not copied schema or ident datoms
  (`peer/src-clj/datomic/index.clj:1831-1845,6270-6290`). `load-index` installs
  lazy index views (`:2081-2169`); startup initializes derived maps and runs
  ordinary database hooks (`peer/src-clj/datomic/db.clj:5495-5545`).
  `ident-setting-datoms` seeks historical AEVT for A=10 and transaction-orders
  assertions (`:5302-5317`); `run-hooks` folds idents before install/alter hooks
  (`:5328-5364`); attribute installation reads the current schema entity
  (`:3123-3128`).
- **Observed native gap:** ATIM copied all current schema datoms and cumulative
  ident assertion history into every immutable manifest. That duplicated
  authority, imposed a 64 MiB metadata ceiling, and made repeated publication
  under ident churn cumulative-quadratic.
- **Repair:** ATIM contains roots and endpoint commitments only. Candidate
  adoption derives the basis-local `IdentIndex` and `Schema` from authenticated
  history/current tree paths before exposure, folds aliases without retaining
  raw ident history, and rejects a candidate whose required metadata path is
  missing or corrupt. This deliberately makes cold startup roots-plus-metadata,
  not the false claim that it reads no child node.

### C27 — Apply `:db/noHistory` to affected merged segments without a base cutoff

- **Docs:** setting `:db/noHistory` affects future indexing jobs and does not
  immediately change current historical values
  (`03_schema/01_changing_schema.md:85-99`). It does not promise that already
  durable adjacent pairs remain forever once a later job rebuilds their range.
- **1.0.7705:** `filter-nohist-pairs` checks endpoint `noHistory`, adjacent
  retract/assert operations, equal E/A, and `common/compare`-equal V; it has no
  transaction/base predicate (`peer/src-clj/datomic/index.clj:2522-2547`). The
  filter receives old-plus-new merged data for each affected segment at
  `:2646-2664,3893-3914`, so its behavior is range-local rather than a global
  retroactive sweep.
- **Observed native gap:** the initial port required the retraction t to exceed
  the base and used stored equality, missing a pair across the base boundary
  and logically equal numeric values with different physical representation.
- **Repair:** use logical index comparison for E/A/V and no base cutoff on the
  supplied affected segment. Ordinary history reads remain exact; only explicit
  consolidation omits eligible pairs.

### C28 — Preserve sparse parent routing without inventing value-size ceilings

- **Docs:** shallow, wide, immutable index trees and independently cacheable
  segments are what allow databases larger than memory
  (`06_indexes/01_index_model.md:79-105` and
  `00_start_here/00_introduction.md:83-100`).
- **1.0.7705:** `make-sparse-lt`, `strdiff`, `vecdiff`, `mindiff`,
  `sparse-datom`, and `sparse-e-xf` synthesize minimum separator keys instead
  of copying complete first datoms into root/directory entries
  (`peer/src-clj/datomic/index.clj:2725-3158`).
- **Repair:** native `RoutingValue` preserves the recovered string/tuple
  algorithm and extends the same comparator-preserving prefix principle to
  legal Rust bytes, URI, keyword, and symbol values. Bytes retain total length
  because their recovered comparator is length-first. This extension is a
  concrete native correction: limiting sparsity to the JVM implementation's
  specialized classes would let otherwise legal 16 MiB native values overflow
  parent nodes and contradict the documented scale property.

### C29 — Use a persistent ordered recent index rather than rebuilding sorted vectors

- **Docs:** every database process combines a recent memory index with the
  durable index, and the memory tier is rebuilt from the authoritative log
  after the durable basis (`06_indexes/02_background_indexing.md:13-19`).
  Immutable database values are persistent values whose predecessors remain
  available (`00_start_here/00_introduction.md:69-78`).
- **1.0.7705:** `datomic.btset` fixes branch and leaf fanout at 16 and implements
  immutable path-copy `conjoin`, split, `seek`, and reverse seek
  (`peer/src-clj/datomic/btset.clj:157-416`). `Db.acceptDataCheck` inserts every
  raw event into EAVT, AEVT, and the eligible AVET/RAET `BTSet`s and appends the
  transaction to `memlog` (`peer/src-clj/datomic/db.clj:4748-4806`); `Db.seek*`
  combines those tier cursors lazily (`:5035-5074`).
- **Observed native gap:** the first `RecentTier` implementation cloned and
  sorted the entire accumulated prefix for every successor and materialized
  range candidates. One-transaction peer advances were therefore
  quadratic-logarithmic over a long unindexed tail, while retained snapshots
  duplicated locator arrays.
- **Repair:** use a small-fanout, immutable `Arc` path-copy search tree for the
  four raw event orders and structurally share the transaction log. Successor
  construction performs logarithmic path work per new datom; seek/range expose
  cursors; current-state add/retract collapse streams over durable and recent
  order rather than manufacturing another complete collection. Rust ownership
  replaces JVM nodes, but the recovered persistence and asymptotic choices are
  retained.

### C30 — Transaction reports are an explicit opt-in observation channel

- **Docs:** `txReportQueue` creates the queue if necessary,
  `removeTxReportQueue` removes it, and an enabled queue grows if it is not
  consumed (`07_peer_api/01_java/02_connection.md`, “txReportQueue” and
  “removeTxReportQueue”; `04_transactions/03_processing_transactions.md:25-38`).
- **1.0.7705:** peer notification constructs a report only for a pending
  submitter or an existing report queue, adds to the queue only when present,
  lazily installs a `LinkedBlockingQueue` in `txReportQueue`, and resets the
  slot to nil in `removeTxReportQueue`
  (`peer/src-clj/datomic/peer.clj:650-715,727-741`).
- **Observed native gap:** every `PeerState` started with a queue and retained
  every transaction observed during open and catch-up, even when the caller
  never requested reports. A long-running ordinary peer therefore had
  unbounded hidden history retention.
- **Repair:** report registration lives as optional connection state outside
  immutable database snapshots. Opening and ordinary syncing allocate no
  reports. Enabling starts an empty queue, removal drops it, and adoption
  enqueues only while enabled after the complete successor state is published.
  Direct transaction responses remain the authoritative submitter result.

### C31 — Give physical root publication an independent monotonic revision

- **Docs:** the transaction log is authoritative and indexes are derived,
  replaceable values (`00_start_here/00_introduction.md:79-109`). Indexing is
  asynchronous with respect to transaction processing
  (`06_indexes/02_background_indexing.md:13-25`), so transaction basis alone
  is not the complete identity of an index publication.
- **1.0.7705:** each merge creates a fresh root id carrying
  `:rev (inc index-rev)` and writes it content-first; visibility is a
  compare-and-set of the storage root reference at its next revision
  (`peer/src-clj/datomic/index.clj:6250-6465`). A competing root revision is
  adopted or causes the candidate to lose, independent of whether another
  transaction was committed.
- **Observed native gap:** PostgreSQL tree manifests/publications are keyed by
  `(database_id, basis_t)`. Once a corrupt derived publication occupies the
  current hard-limit basis, the system cannot publish a repaired candidate at
  that same logical basis and may be unable to admit another transaction to
  obtain a new basis.
- **Repair:** make derived root publication append-only by independent
  database-scoped revision and conditionally advance one root reference.
  Multiple immutable candidates may describe the same transaction basis;
  readers select the newest fully authenticated usable revision. This changes
  no database value or log history and permits idle rebuild/repair. Corrupt
  content-addressed bytes still require detection and restoration or a new
  physical encoding identity; republishing a pointer cannot bless corrupt
  content.

## Internal correctness/evidence requirements

These do not pretend to be Datomic API semantics, but follow from Goal 0's own
production claims: atomic peer state swap on tail failure; coherent PostgreSQL
inspection snapshots; forward migration-version rejection; immutable program
catalog constraints; crash-safe filesystem publication; exact restore
postconditions; and an integration harness that cannot report PostgreSQL tests
as passed without executing them. Derived-state corruption must also be isolated
by database: the log remains authoritative and derived roots are independently
discardable (`00_start_here/00_introduction.md:79-109`), backup identity is
explicitly per database
(`08_operations/01_capacity_and_reliability/02_backup_and_restore.md:32-40`),
and recovered garbage traversal passes the exact `cluster` through lookup and
deletion (`1.0.7705/transactor/src-clj/datomic/garbage.clj:318-390,475-535`).
In the PostgreSQL design, an undecodable manifest for database A therefore may
not abort an otherwise independent backup, integrity, or excision operation for
database B. Shared reclamation must conservatively pin content reachable from
an undecodable root (or fail that reclamation with a scoped diagnosis); it may
not guess reachability and delete data. This is an observed defect, not a
theoretical requirement: after `service_recovery` deliberately persisted a
`5a5a5a5a...` corrupt manifest, both `operations_excision` cases for unrelated
database ids failed at global `delete_unreferenced_segments` with
`encoding/bad-magic`, despite every authoritative transaction payload for those
databases retaining the canonical `41544d43` header. Goal 15 owns the repair and
a cross-database regression fixture.

## Corrected or rejected audit claims

- **Do not require facts for every explicit eid.** The recovered source checks
  the issued frontier, allowing a previously issued entity with no current facts.
- **Do not make a newly installed attribute usable in the same transaction.**
  Ordinary attribute resolution remains against db-before; only final validity
  uses successor schema.
- **Do not rewind operative schema automatically for `as-of`.** Current-basis
  schema remains operative even though schema datoms/history are queryable.
- **Keep pull's documented empty-map result.** Docs
  `05_query_and_pull/03_pull.md:567-577` require `{}` for no pattern match,
  while recovered `pull.clj:81,747,858-865` can nilify an entirely empty result.
  Documentation authority wins unless a narrower runtime distinction is proven.
- **Zero-argument tuple is an authority disagreement.** Docs
  `query_reference.md:1005-1011` say one or more, while recovered
  `extensions.clj:325-330` binds to `vector`, which accepts zero. Treat a
  rejection as a documented-authority decision, not corroborated reconstruction.
- Fulltext, nested `q`, log helpers, random aggregates, EDN/JVM coercion, pull
  transforms, and `qseq` may remain explicitly deferred milestones; they cannot
  be used to claim the complete Goal 0 database is finished if the original
  objective ultimately requires them.
