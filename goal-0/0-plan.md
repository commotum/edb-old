# Goal 0 — Learn from Datomic, simplify Atomic, complete the cutover

## Objective

Make Atomic a readable, source-grounded, idiomatic Rust realization of Datomic's
behavior, architecture and design principles. Preserve strong implementations,
repair demonstrated gaps and unnecessary complexity, and organize the product
so a human can follow a user promise through its rationale into the code.

Deliver the requested inline source WHY commentary, passage-level Pro-doc
traces, coherent shared/peer/transactor components, and chapter-organized user
documentation. This is a full first-release cutover, not a demand to rewrite
every implementation. Working behavior alone does not prove faithful design;
a differently named Rust type alone does not prove a missing capability.

## Authority and constraints

- datomic_pro_docs governs documented behavior. The recovered 1.0.7705 corpus
  supplies architectural and algorithmic evidence. Study mechanisms and their
  consumers, not just API names or artifact directory membership.
- Prefer the source design unless a concrete Rust/platform constraint or
  demonstrated benefit justifies adapting it. Explain the preserved invariants,
  state/coordination boundaries, algorithmic costs and consequences. Neither
  “idiomatic Rust,” “PostgreSQL only,” nor passing tests excuses substitution.
- Existing Rust is evidence, not either the automatic architectural authority
  or disposable work. Choose retain, move, adapt, replace or remove for each
  component. Retention needs a source/contract comparison; replacement needs a
  specific behavioral, architectural, complexity or measured-cost reason.
- Native Rust/PostgreSQL only. PostgreSQL supplies opaque immutable storage and
  conditional references; Rust owns database structures and policy. No JVM/wire
  parity, other-backend project or reconstruction of a runnable JVM distribution.
  Do not port an unused collection merely because its source is bundled.
- Preserve current user capabilities and current-version correctness: exact
  values, schema/identity, immutable values and history, declarative serialized
  transactions, peer-local computation, EDN/application workflows, durable
  acknowledgements, receipt-first exact retries, restart recovery, failover,
  maintenance and backup/restore. Unsupported source evidence is not permission
  to drop a documented core behavior. Atomic-specific features need explicit
  disposition; do not silently remove them or add optional product features.
- Full first-release cutover permits API, path and format changes and fresh
  databases. No old-format readers, compatibility re-exports, upgrade/rollback
  converters, historical-executable matrix or permanent dual implementation.
  Completed component changes may land incrementally; update all current callers
  and remove their superseded paths. Do not reset unrelated databases or erase
  Datomic reference bodies/provenance. Use explicitly disposable live fixtures.
- Separate mechanisms from policies and composition from packaging. A Clojure
  namespace is not a Rust crate requirement. Use coherent modules; use a small
  workspace where crates enforce actual dependency or application boundaries.
  Shared mechanisms must not be copied into both peer and transactor packages.
- Prefer sufficient existing building blocks to new bespoke machinery. Preserve
  efficient structural sharing where it matters: wrapping a whole copied map
  in Arc is not equivalent. Internal mutable cache bookkeeping can be appropriate
  when immutable database values do not depend on preserving cache versions.
- Verification must fit the change and current product. No test/line-count
  quotas, annotation word quotas, historical compatibility obligations or
  invented performance-parity targets. Retain useful independent regressions,
  not tests that merely defend a replaced architecture.

## Evidence at this rewrite

Baseline: 397f5668b36f00f8c07914d895575dfe67b8c562. Recheck against the
execution checkout; this is a bounded assessment, not a whole-product verdict.

- The corpus contains recovered Datomic implementations and exact bundled
  library sources. peer/ and transactor/ are artifact-provenance boundaries,
  not exclusive ownership boundaries. Compare copies before sharing annotations.
- clojure/data/priority_map.clj is the exact data.priority-map library source.
  Static callers include tools.analyzer.jvm.utils → core.memoize/lru →
  core.cache/LRUCache → priority-map. No direct datomic namespace reference was
  found. That does NOT establish “never used,” and static callers do not prove
  a particular runtime path executes it. Datomic's own cache factories here
  use Caffeine. Study dependency mechanisms where relevant; neither dismiss
  them as uninteresting nor turn library presence into a feature requirement.
- Real foundations already exist: src/shared_map.rs implements path-copied AVL
  ownership; src/overlay_index.rs shares datoms across indexes; database-value
  tests exercise long speculative chains, sibling sharing and reclamation.
  src/tiered_assessor.rs is shared by memory, speculative and durable writes.
  src/storage/schema.sql holds opaque objects/references; engine.rs resolves
  matching receipts before fresh assessment. These are candidates to retain
  after comparison, not automatic rewrite targets or proof of complete fidelity.
- Demonstrated design debt: tree_cursor.rs, program_cache.rs and fulltext_store.rs
  repeat map/deque recency bookkeeping. The tree-cache hit scans the deque under
  a shared mutex. This establishes a cost shape and consolidation opportunity,
  not measured application harm or a mandate to use a priority map.
- One atomic-core crate, mostly flat source/docs and many path-attributed modules
  make ownership harder to discover. Existing ObjectReader/ObjectWriter and
  shared traversal boundaries are useful starting points. Folder moves alone
  will not establish sound responsibility or dependency boundaries.
- Latest bounded check: cargo test --offline --lib -- --test-threads=1 reported
  420 passed and 13 failed in the sandbox. All 13 failures passed when the
  affected local-transport and SSD-cache suites were rerun outside filesystem
  ownership/socket restrictions (18 tests reported passed across those suites).
  ATOMIC_POSTGRES_URL was unset: PostgreSQL-dependent cases returned early.
  This is NOT fresh PostgreSQL, application, failover or backup acceptance.
  docs/acceptance.md records earlier live results and limited local measurements;
  do not silently promote those records into current verification.
- User documentation still needs reconciliation: README's retained-handle
  wording must agree with the current grace-based, no-reader-pin contract in
  docs/read-values.md and the actual implementation.

The product has substantial implementation substance but uneven architectural
coherence. Neither “all missing features are resolved” nor “best-in-class
replication” has been established. Current execution status is recorded below.

## Source learning and traceability

**Account for the corpus.** Keep one navigable atlas identifying every source
file's origin, role and counterpart: Datomic implementation, exact dependency,
recovered/generated scaffolding, or non-target integration. Inventory can be
mechanical; substantive reasoning follows coherent components and their callers.
No unexplained “irrelevant” bucket and no repeated analysis of identical copies.

**Explain the WHY in place.** For each distinct Datomic implementation, explain
major namespaces/classes, protocols, structures and functions with marked
ATOMIC-NOTE comments. Cover purpose, consumers, invariants, data flow, mutable
state, coordination, failures and significant cost/tradeoff decisions. Group
small helpers under a shared rationale where appropriate. Read relevant
dependency and Java implementations when they carry the mechanism; do not require
an essay or a replacement for every bundled helper.

Distinguish observed mechanism, documented rationale, inferred rationale and
unknown. Do not invent the authors' thought process or treat decompiler
scaffolding as intentional source design. Preserve code bodies, docstrings,
original attribution and provenance records. Record the unannotated revision;
annotated files must not be represented as still-verbatim archive extracts.

**Trace user promises precisely.** Cover every Pro-doc chapter's important
behavioral paragraphs, rules, table entries and examples. Use a stable passage
locator and implementing artifact/path + symbol + baseline/section or lines.
Place clearly separate notes beside the reference text or use chapter-local
companions linked from those passages. Preserve original wording and URLs.
A namespace-only link is insufficient if the implementing path remains unclear.

Traces may cross components or delegate to libraries. Distinguish implementation,
version mismatch, operational guidance, non-target integration and unresolved
evidence. Link adopted contracts onward to their Rust owners and focused checks.
Treat a documented feature gap, architectural mismatch, optimization opportunity
and packaging difference as different findings, with different remedies.

**Work component-first.** Before changing a component, read/annotate its relevant
source and callers, trace its documented contract, then decide its Rust owner
and disposition. Complete that implementation and exercise its user-facing path.
Unfinished study of unrelated components is not a global prohibition on useful
code changes. Full corpus accounting, major-source explanations and important
passage coverage remain final deliverables; update them throughout the cutover.
Keep decisions in this plan and existing trace records, not competing reports.

## Ordered stages

### 1. Calibrate one end-to-end slice and component map — complete

- **Outcome:** A source-backed route through the working product and a practical
  target ownership map, without a whole-corpus annotation gate.
- **Focus:** Verify provenance and inventory counterparts; pilot entity-map/EDN
  submission through expansion, assessment, durable publication, report and peer
  read. Annotate the actual mechanisms and map relevant doc passages. Compare
  current Rust and name concrete retain/move/adapt/replace decisions. Establish
  the shared/peer/transactor/provider dependency direction and user-doc layout.
- **Completion signal:** A reader can follow that slice through source, docs and
  Rust; strengths, real gaps and unknowns are distinguished. Target boundaries
  and the next component change are justified by callers and contracts. Corpus
  inventory exists; unrelated detailed annotation is not a prerequisite.

### 2. Reconcile shared values, structures and storage — active

- **Outcome:** Coherent shared foundations with small opaque-storage boundaries,
  preserving useful existing mechanisms instead of recreating them by default.
- **Focus:** Apply the component loop to values/schema/identity, persistent maps
  and overlays, log/index trees, codecs, canonical object I/O and reference
  primitives. Separate collection/traversal mechanisms from cache/maintenance
  policy. Study relevant library designs, including priority-map composition,
  without requiring that specific collection where consumers need something else.
- **Completion signal:** Retained or repaired components have source/doc traces,
  clear owners and updated callers. Current semantic/structural checks and real
  PostgreSQL primitive/publication checks pass. Evidence supports selective
  traversal and sharing; SQL does not acquire database-engine policy.

### 3. Reconcile the transactor and lifecycle — pending

- **Outcome:** Readable serialized write processing and reliable operations over
  the shared structures, with one authoritative transaction implementation.
- **Focus:** Expansion/functions, identity resolution and validation, assessment,
  log durability, exact receipts, acknowledgements, novelty/indexing publication,
  writer fencing, recovery, retention, collection and excision. Compare state,
  pipeline and failure boundaries with the source; remove redundant machinery
  only after preserving its real contract.
- **Completion signal:** Source-to-Rust traces explain write and maintenance
  paths. Fresh-database transaction, ambiguous-outcome retry, restart, stale
  writer/failover and maintenance cases pass using the actual service. Completed
  indexing preserves subsequent novelty, and acknowledgements follow durability.

### 4. Reconcile peer-local reads and computation — pending

- **Outcome:** A coherent peer library using shared immutable structures, with
  source-grounded algorithms and an understandable cache/observation model.
- **Focus:** Database values/time views, speculative branches, index/log cursors,
  recent-plus-stored merging, Datalog/rules, Pull/entity navigation, fulltext,
  caching and transaction observation. Address repeated cache bookkeeping based
  on actual consumers. Keep query work independent of a running transactor.
  Indexed/set-oriented leverage is not interchangeable with passing small scans.
- **Completion signal:** Relevant documented examples and application reads pass;
  held values, history, source composition and speculation preserve their
  contracts. Cache changes preserve active reader ownership and have measured
  complete-path costs, including contention/allocation where relevant.

### 5. Finish application boundaries and chapter-organized docs — pending

- **Outcome:** An end user can understand and use the organized product without
  knowing its development history or piecing together internal types.
- **Focus:** Compose peer/transactor/shared modules and justified crate boundaries;
  keep executables thin. Reconcile typed APIs, EDN file/stdin workflows, native
  computation, transports, reports/change consumers, administration and
  backup/restore. Organize docs by the Pro hierarchy: 00_start_here, 01_tutorials,
  02_core_concepts, 03_schema, 04_transactions, 05_query_and_pull, 06_indexes,
  07_peer_api, 08_operations, 09_optional. Use native Rust API chapters rather
  than JVM placeholders; separate development traces from user instructions.
- **Completion signal:** Examples, CLI, rustdoc and README point to current
  owners/chapters. A real PostgreSQL application exercises create/schema,
  EDN and typed transactions, reads, reopen/retry and backup/restore. Reader
  retention and other operational promises agree with code and checks. Peers do
  not depend on transactor runtime ownership; shared code is not duplicated.

### 6. Close source/doc coverage and remove superseded work — pending

- **Outcome:** The requested source commentary and passage-level traceability are
  complete, and no abandoned implementation remains behind the new organization.
- **Focus:** Reconcile the atlas with both corpora and all Pro chapters; inspect
  remaining distinct relevant code and missing promises, not already-explained
  copies. Resolve consequential unknowns and version differences. Reopen owning
  stages for discovered gaps. Remove obsolete code, dependencies, tests, fixtures
  and flat docs; port independent useful regressions and update all callers.
- **Completion signal:** Every source file is accounted for, major distinct
  Datomic mechanisms have WHY explanations, and important passages have precise
  traces or justified dispositions. Relevant gaps are resolved, not renamed
  non-goals. One current implementation remains, with no compatibility aliases,
  dead fallback path, misleading provenance or broken documentation links.

### 7. Establish integrated product acceptance — pending

- **Outcome:** One source-grounded, maintainable, working native product with
  an honest statement of verified behavior and operating limits.
- **Focus:** Run proportionate final semantic, architectural and application
  checks on the final tree. Exercise actual PostgreSQL durability, restart,
  exact retries, failover, maintenance, backup/restore and transport security
  where supported. Inspect dependencies and mutable-state boundaries, not just
  directory names. Measure representative full write/read/indexing paths at
  meaningful sizes before claiming scalability or an improvement.
- **Completion signal:** A human can follow docs → annotated source → Rust owner
  → working example/check. Current-version integrated workflows pass without
  skipped prerequisites being counted as acceptance. Costs and limits are
  explicit; all required coverage, cleanup and capability outcomes hold. Reopen
  the owning stage for failures; do not create a corrective parent or silently
  narrow completion.

## Status and continuation

Stage 1 completed on the execution baseline cd7192e63d883a4a34aa7de4d5bcd17e6edb692d.
The development/source atlas inventories 493 source files with origin, baseline
hash and counterpart comparisons. Eight pilot source files now have inline WHY
notes; the preservation check confirms comment-only changes. This is not full
symbol coverage. The chapter-local 02_transaction_data.atomic.md trace connects
map-form passages to source functions, current Rust owners and expected results.
The atlas records module-first ownership, retain decisions and two next issues:
nested-map identity guards (Stage 3), and synchronous versus pipelined write
processing (Stage 3). These are not silently treated as equivalent.

Fresh Stage 1 checks: built atomic/native_workflow and EDN test executables.
On isolated PostgreSQL 16.15, the actual_edn_commands_install_transact_preview_query_pull_history_and_retry
CLI workflow passed, followed by all 9 edn_transactions cases with PostgreSQL
configured. The CLI confirms real maps/nesting, omitted-attribute preservation,
preview isolation, history/source joins, restricted roles, writer-independent
reads and exact retry after writer restart. Debug complete transaction processes
for 32/128/512 added entities took 208490/312017/693514 microseconds in this run;
query processes took 63391/93569/163964 and Pull 58907/65404/108007 microseconds.
These are local samples, not throughput or performance-parity claims.

Stage 2 is active. Shared collection repair is integrated: the private
collections::LruMap uses HashMap lookup plus reusable doubly-linked slots. Tree,
program and fulltext consumers keep their policy/counters and Arc ownership;
no persistent priority-map or Caffeine-policy port is claimed. SharedMap's real
path-copying AVL implementation moved to collections/persistent_map.rs, with
current callers changed and no forwarding alias at the deleted old path.

Fresh checks: all 4 collection tests pass (including independent randomized
ordering/slot-reuse oracle and retained AVL snapshots). The host-permitted
20-test cache filter passes with PostgreSQL configured. Its first sandbox run
had 8 SSD directory-ownership failures and 2 PostgreSQL skips; these are not
accepted as product results. On the actual server, all 12 block_storage tests,
3 block_fulltext tests, the block_program_cache test and the EDN CLI workflow
pass. The fulltext policy regression is separate from that name filter and
must be included in the next focused/full library check.

Measured debug cache lock/get/Arc/drop hot-hit means at 32/128/512/2048/4096
residents were 1625/5671/9200/35801/70450 ns before the repair, versus
1086/1083/1062/1049/575 ns after. These noisy local samples support removal of
the recency scan, not a whole-query speedup claim. New non-MRU contention cases
at 4096 residents measured 4083 us for 2048 hits/one worker and 24840 us for
8192 hits/four workers, including thread start/join. Both preserved accounting
and values. Slots are reused but buffer capacity follows peak occupancy;
cache-owned decoded-byte counters are not process RSS limits.

The 512-record read_load application campaign completed with zero reported
errors and cleaned up its schemas/roles. Warm one-peer reads used no foreground
SQL (3165 operations/500 ms, p50 155 us, about 23 MB peer RSS in debug). It ran
alongside focused checks, with uncontrolled warm PostgreSQL/OS caches: not a
controlled before/after comparison. The post-repair EDN 32/128/512-entity full
transaction processes took 228138/284975/710177 us, query 54482/73165/167103 us,
and Pull 58826/64593/112081 us. These show the application paths still work,
not that the bookkeeping change accelerates all queries.

The separate 2048-record read_load run (512-byte payload, 500-ms phases, debug,
uncontrolled warm PostgreSQL/OS cache) also completed and removed its isolated
schemas/roles. Warm 1/2/4-peer phases performed 3190/5940/10890 operations with
p50 153/167/176 us and no foreground query SQL. Approximate aggregate peer RSS
was 23/46/92 MB; full scans exceeded the 1 MiB local cache and did storage reads.
This demonstrates working peer-local reuse and limited local read scaling, not
production throughput. Complete seed took 13.175 s/4648 driver SQL calls; mixed
writer phases took about 100 ms median with 710 calls/3 writes and 1195/5 writes,
including maintenance. Publication/encoding amplification remains a real Stage 3
cost issue to explain and address, not something the cache improvement closes.

Next active component: shared model URI equality/order/hash. Source comparison
found raw-string URI equality differs from source component semantics, affecting
identity/lookup; preserve original encoded spelling while fixing logical value
behavior. Opaque SQL/provider study is in its chapter-local trace; guarded
publication/collector design and remaining foundation ownership still need
comparison. Stages 3–7 and complete source/doc coverage remain unfinished.

Disposable live fixture (leave unrelated databases alone):
host=/tmp/atomic-cutover-pg.GbbJ4R/socket port=56147 dbname=atomic_cutover user=jake.
It has fsync, synchronous_commit, full_page_writes and data checksums on; TCP is
disabled. Binaries are under prefix/usr/lib/postgresql/16/bin inside that task
directory; LD_LIBRARY_PATH points to prefix/usr/lib/x86_64-linux-gnu there.
This fixture was provisioned from Ubuntu packages without system installation.
Host-permitted test execution is required for local sockets. Stop this owned
server when the execution session no longer needs it; do not delete other data.

Keep one active stage/component and one integration owner. Replace the
continuation note with material decisions, actual checks, uncertainty and next
action at session boundaries. Archived goals are evidence only.

Completion requires the entire product cutover AND the requested learning and
traceability deliverables. An annotation campaign, folder reorganization,
scaffold, passing test count or completed individual stage is not the finish line.
