# Goal 0 — Learn from Datomic, simplify Atomic, complete the cutover

Status: complete. All seven stages are complete; final acceptance and limits are
recorded at the end of this plan.

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

### 2. Reconcile shared values, structures and storage — complete

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

### 3. Reconcile the transactor and lifecycle — complete

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

### 4. Reconcile peer-local reads and computation — complete

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

### 5. Finish application boundaries and chapter-organized docs — complete

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

### 6. Close source/doc coverage and remove superseded work — complete

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

### 7. Establish integrated product acceptance — complete

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

Stage 2 is complete. Shared collection repair is integrated: the private
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

Shared model repair is now implemented and its focused integration verified.
The actual value/datom/schema/ident/vocabulary/coordinate code lives under
src/model, including the independent numeric oracle; private old module paths
were removed, while the curated root public type facade remains intentional.
URI syntax is checked at EDN, native admission, schema and canonical decoding
boundaries. Comparison/hashing use URI components without normalizing stored
spelling. Native authority-kind ordering deliberately fixes a source-platform
ordering-law problem; it is documented, not claimed as byte-for-byte Java parity.
The deeper tree review found and removed raw-text URI sparse separators: exact
URI keys now work recursively inside tuple separators. This costs parent bytes
for long URIs and retains explicit tree capacity limits. Opaque-block envelope version 2
and tree version 5 reject older development comparison/layout generations;
canonical value spelling remains exact. No database was reset or converted.

Source TupleElem also exposed UTF-16 versus Unicode-scalar length and signed
BigInteger bit-length boundary differences. Shared schema admission now uses
the source's units. The scalar 8192-bit interpretation is documentation-backed;
its recovered scalar guard remains unlocated. Unique-bytes rejection is retained:
the specific documented Bytes limitation resolves the broader identity prose,
so it is not an omitted feature. Physical AVET readiness remains separate from
schema intent. These decisions are in the schema and index chapter companions.

Fresh focused checks: all 10 tuple_schema_repair and 10 uri_values tests pass,
including real PostgreSQL URI consolidation/reopen, immutable before/after
receipts, receipt-first retry and changed-spelling request rejection. The first
retraction test wrongly required the earlier assertion's spelling: source
ProcessExpander constructs a new retraction from submitted v. Corrected that
expectation, not the working behavior. A tuple test's hardcoded empty basis was
also corrected to account for installed schema. These failures were test errors,
not silently removed assertions. The library run with PostgreSQL configured and
host socket/SSD permissions passed all 445 tests in 249.91 seconds. It includes
the URI multi-leaf/tuple routing, codec admission, exact-number oracles, fulltext
cache policy, publication/failover, collection and backup checks. This run covers
the model/routing repair before the following index/module and ObjectWriter
changes, not their uncompiled state or final product acceptance. Source
preservation currently accounts for 493 files and confirms 27 comment-only
modified source files.

The subsequent foundation cutover is now compiled and verified. index/ owns
boundary/eager/recent/overlay, cursor, tree/cache/navigation, metadata and edit
preparation; metadata no longer depends on preparation. Current callers and
subprocess test selectors use the new owners, with no old module aliases or
path-attributed test wiring there. encoding/ owns canonical bytes and its
program/query/submission children; physical compression is storage::codec.
Keep those layers separate rather than moving higher-level protocols into model.
Log/receipt builders use ObjectWriter instead of a concrete PostgreSQL driver.
Forward log ranges retain ancestor pages rather than reseeking the tail per page.
That gives O(Q + log P) page fetches and O(log P) retained pages; skip arrays
vary in size, so this is not a constant-memory or whole-query speedup claim.

Fresh post-cutover checks: cargo check --offline --all-targets passed. The full
library suite passed all 448 tests in 251.41 s with real PostgreSQL and host
socket/ownership permissions. Then block_log (5), block_receipts (4),
block_storage (12), and the actual EDN CLI workflow passed with PostgreSQL
configured. This includes maximum-current-size chunked log/receipt cases,
provider corruption/reuse, root CAS, restart/retry and restricted reader roles.
The forward-range comparison independently matched the old point-seek path at
8/13/32/65/128 pages, with full/partial/empty/early-stop and fused-error cases.
At 128 pages, page reads fell from 448 to 127 (tail already captured). The real
18-page range read 1091 entries with exactly 1108 SQL calls/135550 payload bytes
in 719247 us. Local debug timings used uncontrolled warm OS/PostgreSQL caches;
some checks overlapped, so these are operating samples, not controlled latency
improvement claims. The post-cutover EDN 32/128/512-entity transaction processes
took 188078/301661/661575 us. Source inventory still accounts for 493 files,
with 35 comment-only annotated files at this checkpoint, not full coverage.

Opaque SQL/CAS/protection primitives were compared with their consumers: keep
guarded monotone protection and tombstones, not engine policy in SQL. Fixed stale
comments that claimed racing reads must conflict, reads persist wrappers, or
content existence proves a protected-put attempt. Read handles use durable
ownership plus retirement grace, not reader pins. Serialization study retains
exact decimal scale and URI spelling but explicitly records existing canonical
float zero/NaN handling; bundled datomic/data is a permutation table, not a
missing value-model implementation.

Stage 3: transaction expansion, shared assessment and durable write preparation
are integrated. Explicit nested-ID admission is repaired, preserving native
unique-value/reverse-map capability and documenting the source/doc distinction.
All 11 edn_transactions and four edn_cli cases pass with real PostgreSQL. The
new CLI cases update a noncomponent child by numeric/lookup ID via stdin/file,
preserve omitted facts and held values, and replay exactly after restart. One
new reverse-map fixture initially introduced a value-only tempid: corrected the
fixture and retained the engine's rejection as an independent assertion.

Path compression now preserves minimum identity representatives while removing
the demonstrated quadratic parent walk. Independent chain and actual-assessment
tests pass, including partition/upsert conflicts and immutable predecessors.
At 32/128/512 tempids, old-reference parent steps 992/16256/261632 compare with
184/760/3064 compressed steps. Complete debug assessment/drop took
2.55/10.80/48.95 ms, separate from setup 189.80/448.46/1632.21 ms. These are
sequence-specific bounds/samples, not whole-product scalability claims.

Guarded immutable uploads are now grouped behind an explicit persistence barrier;
this is not the source's multi-stage/multi-transaction pipeline. Nine buffer
regressions pass, including bounded groups, read-your-writes, sticky errors and
no Drop I/O. Both real-provider batch tests pass. Existing writer fence/recovery,
lost-ack, final-GC-CAS and receipt-first regressions pass. The new prepublication
witness initially assumed all objects used ATOB: tree nodes use ATIX. Its scan
and traversal were corrected to respect canonical object families; the corrected
witness now passes on real PostgreSQL in the post-move library run.
Before batching, the complete service-phase check passed with 114 SQL calls
(about 46.86 ms total/36.03 ms encoding/8.18 ms commit in that local sample),
and 17 replay calls; it covered rejected publication, unknown-outcome recovery
and once-only callbacks. The same check now passes with 32 SQL calls and 17
replay calls, about 15.40 ms total/7.06 ms encoding/6.51 ms commit in a local
debug sample. Different concurrent activity/cache conditions mean no controlled
latency ratio is claimed. Object/guard round-trip reduction is directly measured.
Transaction and service organization is now implemented and verified: forms,
normalization, callbacks, input validation, assessment and clock/pipeline have
transaction owners; authority, activation/standby, admission, write scheduling,
index/excision lanes and reports have transactor owners. Catalog identity/genesis
and generic GC guards no longer import writer ownership. Administrative excision
claims/drives/releases its writer from transactor/excision_operator; shared jobs
only prepare resumable candidates. Obsolete engine/service/runtime paths are
removed, without compatibility aliases. Local commit hints are bounded channels,
not generic callbacks invoked on the writer. Their loss is repaired by durable
peer catch-up; the separate lossless report contract is unchanged.

Fresh verification: all-targets check passed after these moves (10.53 s), formatting
and diff checks pass. The host-permitted PostgreSQL library run passed 463 tests
in 266.85 s. All 46 selected integration tests passed: automatic_excision (1),
block_gc (2), block_service (2), block_transactions (8), edn_cli (4),
native_transaction_functions (6), program_transactions (6), service_leadership (3),
service_standby (5) and service_worker (9). The final administrative-driver move
was compiled and its exact operator checkpoint/restart regression rerun (1 pass).
The 5 MiB live transaction crossed upload groups/two log chunks and reopened with
its exact receipt (6.89 s complete debug sample). The actual EDN workflow verifies
map updates, immutable held values, source joins, preview and writer-independent
reads, restricted roles and restart/retry. Concurrent test activity makes these
durations operational samples, not controlled performance comparisons.

Recovery and excision now reuse the forward log cursor rather than reseeking
per entry. New real-path regressions pass: 130-transaction recovery reads three
pages five times total (tail once, sealed pages once for validation and once for
replay), with 412 complete driver calls; excision reads 130 source entries plus
two pages, with 14,896 total driver calls including immutable rewrite/receipt
work. Entry size rejection happens before chunk reads and leaves its checkpoint
unchanged. This reduces navigation, not whole-history rewrite costs. Native
excision still pauses fresh writes through rewriting; GC is bounded per call
but scans all namespace objects per cycle. Both are explicit source adaptations
in the lifecycle traces, not hidden scalability claims.

The isolated block_live_costs application sample also passed, with no concurrent
test or compilation workload during execution: 2,048 entities/1,572,864 scalar
payload bytes versus 1 MiB configured caches. Startup through shutdown took
13.73 s/1,889 SQL calls. Seed commit took 2.10 s and seed plus automatic indexing
8.24 s; 24 small writes took 0.543 s/1,062 calls, and writes plus automatic
indexing 4.74 s. Three automatic jobs ran; subsequent 32 warm reads took 697 us
with zero SQL. This includes actual write/index/sync/shutdown, with uncontrolled
warm OS/PostgreSQL caches in debug; byte accounting is not RSS. Together with
the fresh fault/restart/maintenance checks, this closes Stage 3, not the parent.

Stage 4: immutable database values and peer capture/observation are integrated.
The shared `database_value` owner now separates exact value/metadata, temporal
views, ordered cursors, speculative overlays, identity resolution and discardable
attempt read context. Public log/reference/hint adapters live beside these values.
`peer::{live,snapshot}` captures and advances them; optional embedded-service
composition belongs to `application::connection`. Stable DatabaseIdentity is
a model value, no longer a reason for writer code to import a Connection.
Root public APIs remain intentional; superseded internal file paths are removed.
The resident transaction instant is a direct Option, not a lock/OnceLock memo.

Two source comparisons produced substantive repairs. Schema/index synchronization
now waits only for relevant work through the requested T, deriving its initiating
schema transition from existing history. It separately checks the frozen partial
descriptor and later tail, so later work cannot extend an older wait or hide an
unfinished earlier checkpoint. Existing excision readiness was already targeted
and is retained. Report-enabled catch-up now carries exact authenticated endpoints
forward instead of reconstructing both recent prefixes for every report. Changed
physical index/generation roots are still opened in full; equality of logical
SnapshotKeys cannot substitute physical endpoint identity. Whole-batch validation
still precedes peer publication/report delivery.

Public log scans now share the forward traversal used by storage/recovery rather
than reseeking per transaction. The reader-independent navigation state permits
an owned snapshot cursor without unsafe self-reference or a duplicate algorithm;
the same authenticated, generation-scoped SSD payload reader remains in use.

Fresh Stage 4 checks: the real-PG library run passed 463 cases and failed only
the new readiness fixture's illegal uniqueness-before-index request. Corrected
that fixture to assert the rejection, then exercise legal index-first admission;
its focused rerun passed (1, 2.80 s). No engine rule was relaxed. All 46 selected
integration cases passed: block_peer (3), database_value (8), native_connection
(7), native_log (5), native_log_cache (2), native_speculation (3),
native_time_points (5), query_runtime_sources (10), snapshot_references (3).
The final block_peer rerun (3) additionally passed a same-basis physical index
rebase between exact receipts, and native_log (5) reran with cleanup included.
Held values, source-log provenance, actual restricted reads, restart-hot cache,
corruption, ordered reports and two excision-generation handoffs are exercised.

Report catch-up at 16/32/64 missing transactions took roughly 350/740/1,580
SQL calls; an additional 16 already-held transactions did not multiply replay.
Exact calls/bytes and concurrent debug timing samples are in the deployment
companion, not a claim of source throughput parity. The final log sample used
130 transactions across three pages: point reads 257 calls/284,489 result-cell
bytes versus forward traversal 132 calls/20,348 bytes. Setup 4.20 s, point
156 ms, forward 79 ms; complete startup/write/shutdown/read/drop/fixture cleanup
4.45 s. This is a debug sample with uncontrolled PostgreSQL/OS warmth, not an
RSS, cold-read or controlled latency-ratio result. Source preservation currently
accounts for 493 files with 46 comment-only annotated files, not final coverage.

Stage 4 Datalog query execution and rules are now integrated. The former flat
query owner and sixteen helper files were cut over without compatibility aliases
to `src/query/`: typed syntax, controls, execution, bindings, patterns, rules,
results, sources, preparation and extensions have explicit owners; specialized
join/range/numeric/nested/diagnostic/value mechanisms remain separate children.
Moved helpers were retained where their algorithms already fit the source-backed
contract. The prepared-query cache now reuses the shared keyed linked-slot LRU
while retaining its existing admission weights, hit/miss/eviction semantics and
invocation-local values.

Two demonstrated behavior/control gaps were repaired. `distinct` now returns a
public `QueryValue::Set` and readable EDN set, matching `aggregation/distinct`
and the Pro example rather than merely returning the same members in a sequence.
Lazy query Pull now carries the caller's original `max_value_bytes` through
deferred projection instead of replacing it with an unlimited allowance. New
typed and EDN regressions exercise grouped duplicates/`:with`, exact result shape,
failure at a small lazy allocation limit, iterator fusion and successful reuse.

The chapter-local query companion traces important execution/reference passages
to recovered symbols, new Rust owners and focused checks. It records native
bound-score scheduling and controlled unbound EAVT scans as explicit adaptations,
not source equivalence, and preserves the stronger exact numeric implementation.
The source guard accounts for all 493 files with 56 comment-only study changes.
Fresh verification after the cutover: `cargo check --offline --all-targets`
passed without warnings; 49 focused result/composition tests passed; 36 selected
query/source tests then passed with PostgreSQL configured; the broader rules,
negation, ranges, preparation, diagnostics, numeric and custom-aggregate run
passed all 76 tests. Finally the PostgreSQL-configured library suite passed all
464 tests in 68.91 seconds. These establish this component on the current tree,
not final product acceptance.

Stage 4 Pull and entity navigation are now integrated. The flat Pull, Index Pull
and bounded-test owners were cut over without compatibility aliases to
`src/pull/`: public model, controls, entity navigation, iterative execution,
attribute preparation, transforms and lazy index projection have distinct
owners. Entity identity remains a shared model concern because database values,
recovery and backup consume it independently. The existing explicit continuation
machine, stack-safe clone/drop, immutable-value capture, recursion/limit controls
and cursor implementation were retained after comparison rather than rewritten.

Five source-backed gaps were repaired. Bare reverse component selectors now
return the singular parent id map and expand it only for an explicit nested
selector. Failed entity resolution still processes each selector's transform and
then default without inventing wildcard attributes. Index Pull polls cancellation
inside filtered/rejected traversal and fuses the failed cursor. EDN underscore
selectors use the captured schema: an exactly installed `_name` is forward,
otherwise the spelling denotes reverse `name`; the public typed representation
names this conditional behavior `PullDirection::SchemaResolved`, with an explicit
current-format codec tag. Finally, reverse associative navigation preserves an
identified incoming entity instead of applying the forward-only ident-as-keyword
presentation rule.

The Pull companion traces the relevant Pro promises and recovered `pull.clj`,
`query.clj`, `db.clj` and Java Entity mechanisms to the new Rust owners. Fresh
verification passed all-target compilation, the source-preservation/link and diff
guards, 62 focused integration cases with PostgreSQL configured, and eight
bounded internal cursor cases. The 62 cases include typed and EDN contracts,
actual durable Index Pull/entity identity, held/time/filter views, transforms,
deep component traversal and all five repairs. The source guard now accounts for
all 493 files with 60 comment-only study changes. The final public variant rename
was followed by another all-target compilation; it did not alter runtime logic.

Stage 4 fulltext search, publication and immutable caching are integrated. The
flat implementation was cut over without aliases to `src/fulltext/`: public
model and cumulative controls, eager/native search with exact-view validation,
analysis, source-bound readers, logical records/differences and the authenticated
search tree now have visible owners. Search-tree model, page codec, positive
cache, cursor, bulk builder and incremental path editor are separate internal
modules. PostgreSQL/object I/O and coherent index publication remain under
`storage::fulltext`; the Datalog bridge remains under `query::fulltext`.
Existing analyzer, page encoding, cache and incremental algorithms were retained
body-for-body apart from imports, sibling visibility and formatting.

One independently demonstrated control gap was repaired: exact-view candidate
validation now advances its prefix cursor through the shared callback, so custom
filters cannot hide an arbitrarily long rejected history from cancellation or
the direct/query work budget. The adversarial regression failed on the prior
code and now passes for both `FulltextOptions` and enclosing `QueryControl`, then
reuses the same immutable view successfully. Source comparison retains native
versioned grammar/BM25, exact E/value/tx validation, post-validation truncation,
and synchronous durable+recent+speculative answer composition as documented
adaptations; Lucene ranking/file parity is not claimed. The nonexistent separate
idle search worker is no longer represented by an always-zero public statistic,
tests, diagnostics or docs.

The fulltext companion traces Pro schema, Datalog, eventual indexing and cache
promises through recovered fulltext/fulltext-index/Lucene mechanisms to current
Rust owners. Source preservation now accounts for all 493 files with 74
comment-only study changes. Fresh verification passed all-target compilation,
diff/link/preservation guards, 20 focused integration cases with PostgreSQL
configured, 24 fulltext internal cases, and three projection observation/retry
cases (including real durable recovery). The final PostgreSQL-configured library
suite passed all 464 cases in 265.86 seconds. Together with the earlier immutable
value, peer/log, Datalog and Pull evidence, this closes Stage 4, not the parent.

Stage 5 application boundaries and chapter-organized end-user docs are now
integrated. No new crate was needed: module ownership is sufficient for the
actual dependency boundaries. `runtime::executor` owns the generic bounded
worker/resource lifetime used by async applications and deferred storage cleanup;
`application` owns connection composition, async client/streams and local/remote
transports; `observation` owns cross-cutting hints and durable change consumption.
`operations` owns administrative inspection/index/reclamation/deployment policy,
while storage/transactor retain their semantic workers. `backup` owns capture,
repository publication, immutable read points, verification and restore.
Immutable stored-function invocation remains with `database_value`. PostgreSQL
environment parsing remains provider-owned. The binary is thinly divided under
`src/bin/atomic`; curated root exports remain the public API, while superseded
internal flat paths were removed without aliases.

The canonical backup replay now retains `LogTraversal`'s authenticated forward
anchors instead of calling `read_record` once per basis. The independent old-path
oracle read 832 sealed navigation pages for the fixture versus eight with forward
replay; each forward page was read once and the captured tail needed no read.
History equality, cancellation at an interior basis and a missing next object
are permanent regressions. Sparse receipt and retained-value authority checks
remain point reads because their order is not chronological; no whole-verifier
linear-I/O claim is made. The application fulltext example also stopped retrying
a missing attachment against the same immutable value: one controlled call now
returns a complete value-relative answer or a clear failure.

User documentation now occupies the ten chapter families required by the plan,
with 29 current guides and no flat forwarding stubs. Historical acceptance prose
was removed; reproducible validation material lives under `development/validation`.
README, lifecycle, query/fulltext and consumer guidance now agree with actual
no-reader-pin grace retention, recent-tail search completion, exact retries and
checkpoint-write authority. Optional SSD/hint/log caches are separated from core
requirements. Three permanent checks validate chapter coverage, active relative
links/anchors, rustdoc/example paths and stock CLI help. Archived historical links
are evidence and were not rewritten as current guidance.

Fresh verification passed warning-free all-target compilation, diff and active
link checks, 22 focused internal backup/runtime/transport cases, and 52 distinct
PostgreSQL-configured application/integration cases covering async clients,
local/remote submission, routing, change consumers, EDN/product/admin CLIs,
connections/failover and backup/restore including fulltext/program dependencies.
The product workflow initially failed its explicit prerequisite because the
external example binary was not built; after building the stock binary and
example, all three product cases passed. The moved-body audit compared 417
existing functions: production bodies were unchanged apart from approved paths,
visibility and the canonical replay repair. This closes Stage 5, not the parent.

Stage 6 is complete. Cleanup removed the obsolete CLI `migrate` alias, dead API
paths (including the eager Entity constructor and unused raw SQL-client escape),
and the unused SHA feature configuration. Current callers use the retained public
APIs; no compatibility implementation was added. Source/API study closed the
remaining consequential peer, async, cache, backup, capacity and catalog families.
The recovered peer's bounded `queue/put` may block on admission; Atomic's bounded
Busy admission is an explicit native policy, not source-equivalent behavior.

The source and passage ledgers now account for all 493 source entries and all
90 original Pro chapters. `inventory.py --check` permanently rejects missing,
extra or duplicate rows, empty dispositions and nonexistent owner/trace paths;
manual dispositions are never regenerated. Its final Stage 6 run verified
99 comment-only source files with every recovered body preserved and 686 local
trace links resolved. Five focused ledger checks passed, as did the diff guard.
The current-path atlas distinguishes exact dependencies/resources, identical and
nonidentical artifact copies, native counterparts/adaptations, and justified
host/vendor/version-specific non-counterparts. Family-level accounting is not a
claim that every source symbol is annotated or every Datomic product is ported.

Fresh integrated Stage 6 evidence includes 21 focused core checks, the central
binary's seven of seven socket tests, and both central live PostgreSQL
install/refusal tests. Genuine residual unknowns concern arbitrary JVM host and
classloader behavior, recovered metadata/version discrepancies, unclaimed
Caffeine/Valcache/vendor scheduling, and exhaustive cross-process failure
schedules. These are not identified gaps in advertised native behavior and do
not imply whole-product parity or measured source throughput equivalence.

Stage 7 and the parent goal are complete. After the explicit prerequisite
`cargo build --offline --bin atomic --examples`, the clean serialized
PostgreSQL-configured `cargo test --offline --all-targets -- --test-threads=1`
run exited 0 across 1,191 registered tests: 466 library tests, seven binary tests,
and the integration/example targets. An earlier incomplete run exposed the
missing standalone example binary; it is not counted as acceptance. Dedicated
fixtures supplied evidence that the broad suite alone cannot establish: the
PostgreSQL crash witness passed 1/1 in 32.20 s, checking exact retry, rollback and
held values; the TLS witness passed 1/1 in 2.67 s with two TLS 1.3 listeners,
untrusted-root rejection and plaintext rejection.

Final formatting and all-target compilation checks passed. The three current
documentation/navigation/help checks passed, and the source/ledger guard again
verified 493 preserved source bodies, 99 comment-only files, 493 source
dispositions, 90 chapter dispositions and 686 resolving local trace links.
The release-facing audit removed three obsolete historical-comparison sentences
without changing operating limits; README, guides, examples and rustdoc navigate
the current cutover. The diff guard passed.

The final `BLOCK_LIVE_COST` sample measured startup through shutdown at 13.193 s
and 1,868 SQL driver calls. Twenty-four writes took 0.500 s, or 4.599 s including
automatic indexing; three automatic jobs ran, and 32 subsequent warm reads used
zero SQL calls. This is a local debug run with uncontrolled warm PostgreSQL/OS
caches, not a scalability result, RSS bound or performance-parity claim. Current
reproduction prerequisites and acceptance evidence are in
[development validation](../development/validation/README.md).

The central disposable live fixture at `/tmp/atomic-release-pg.rRGLRj` was
stopped after acceptance. This is fixture cleanup, not product deployment, and does
not authorize deleting unrelated data. The source-study uncertainties listed
above remain explicit: completing the advertised native cutover does not claim
JVM/vendor parity, exhaustive failure schedules or production-scale performance.
