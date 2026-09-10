# Goal 0 — Native capability completion

## Objective and authority

Implement the useful native Rust/PostgreSQL capabilities identified in
[the documentation audit](1-audit.md), in dependency-ordered stages, for application
developers and operators. Equivalence means the information model, observable
behavior and useful workflows—not a literal translation of Clojure internals,
JVM limitations or vendor packaging.

The user's 2026-09-10 request authorizes implementation and indexed child goals.
It supersedes this folder's former audit-only loop. Preserve the completed
90-document audit as historical evidence; its static findings are hypotheses or
observed API gaps, not automatic commands to copy every named component.
[Goal 1](../goal-1/0-plan.md) completed EDN and remains intact.

## Current state

Stages 1–4 / Goals 2–5 are verified; Stage 5 / Goal 6 is now active.
The worktree was clean at the start of this request. No audit-stage reread,
replacement engine, recursive hierarchy or corrective parent is required.
Only one child is active; bounded parallel tasks within it are welcome.

Reconciled evidence:

- Goal 1 delivered standard EDN, adapters, CLI and actual PostgreSQL acceptance.
  Goal 4 completed Q02 arbitrary-width relations and Q04 general query data,
  prepared inputs, pure callbacks and the database-free CLI path.
- Q03, P01, P03, ST-05 and P06 are read-API seams over existing mechanisms.
- ST-03 is reproduced through pure and actual PostgreSQL paths: named partition
  allocation fails at ordinary frontier 524,288 and schema at 1,048,577. The
  independent reserved allocator now passes pure and PostgreSQL million-ID
  workloads and upgrades the retained pre-repair database without reseeding.
  Recovery/COW/backup/application compatibility acceptance passed, including
  genuine pre-repair fixtures and complete generation-zero conversion.
- Recovered composite identity ordering supports the current explicit-composite
  upsert-hint behavior (SC-01). Do not invent constituent-only upsert for parity.
  Safe native NaN replacement (SC-02) should not acquire a JVM comparison problem.
  Both now have permanent eager/native PostgreSQL regressions and
  docs/schema-identity.md; no engine semantic repair was needed.

## Non-negotiable constraints

- `datomic_pro_docs` governs documented semantics; `1.0.7705` provides architectural
  and algorithmic evidence. Read relevant original passages for consequential
  decisions. Prefer idiomatic Rust enums/traits, iterators, immutable sharing and
  explicit error/result types over copying Clojure's implementation machinery.
- Preserve existing typed APIs where possible with additive interfaces. Preserve
  immutable values, schema/identity, historical facts, canonical durable bytes,
  old program encodings, exact receipt-first retries and prior repairs. New
  persistent representations need explicit versioning, compatibility and recovery.
- PostgreSQL remains durable storage. No JVM/code evaluator, alternate durable
  backend or universal wire parity. Named functions do not serialize arbitrary
  native closures; deployment and trusted callback limits must be explicit.
- Validate suspected gaps against the usable public workflow. A native equivalent
  counts; naming differences and performance knobs alone do not establish defects.
  Conversely, do not hide a core gap as an unsupported convenience or call it done
  because a private helper exists.
- Every required stage has public regressions, an evolving end-user example,
  relevant real PostgreSQL/application checks, and measured complete-path costs.
  Skips are not passes; work accounting is not RSS; single samples are not
  throughput certification.
- Preserve user/concurrent changes. Destructive lifecycle/excision tests use
  explicitly isolated fixtures. Never clean shared data or rewrite old identities
  to make a test pass.
- Keep records concise. Create/reconcile exactly `0-plan.md`, `0-loop.md` and
  `0-prompt.md` in the matching child with scaffold-goal. Do not replace completed
  child work or create nested goal folders.

## Implementation stages

Stages 1–9 map to Goals 2–10; Goal 1 is completed EDN evidence, not renumbered.

### 1. Composable immutable read APIs — Goal 2

**Status:** Complete; public APIs, regression and actual PostgreSQL/application acceptance verified.
**Audit ownership:** Q03, P01, P03, ST-05, P06.

**Outcome:** Applications compose captured database, tuple and log sources, navigate and compare entities, inspect index readiness, author virtual tuple boundaries and invoke stored native functions without custom plumbing.

**Focus:** Reuse QuerySequence, DatabaseValue, existing index normalizers and authenticated program resolution. Preserve eager joins/aggregation with lazy Pull/transforms; no requirement to stream the whole evaluator.

**Completion signal:** Mixed-source lazy reads agree with eager results and preserve exact bases, cancellation and error fusion; entity identity is stable across time but not unrelated lineages; partial tuple seeks and physical readiness work; direct invocation uses captured bindings and read-only authority. Permanent public-API and real PostgreSQL/application checks pass with bounded complete-path costs.

### 2. Sustainable schema and identity evolution — Goal 3

**Status:** Complete; public behavior and integrated compatibility verified.
SC-01/SC-02 verified; ST-01 API/PostgreSQL/CLI checks pass. ST-03 native and
retained old-format million-ID workloads pass; old program/partition/identity
fixtures, backup/recovery/COW, exact retries and application checks also pass.
**Audit ownership:** ST-03, ST-01, SC-01, SC-02.

**Outcome:** A long-lived database can add schema and named partitions after ordinary data growth, with safe default placement and documented identity/numeric semantics.

**Focus:** First reproduce ST-03 at both boundaries; separate reserved-schema allocation from ordinary issuance without rewriting existing EIDs, facts or receipts. Define persisted/recovered allocation and explicit/default/match precedence. Validate composite-upsert and NaN candidates before changing behavior; retain superior native semantics when correct.

**Completion signal:** Boundary workloads demonstrate automatic schema/partition installation beyond ordinary-data frontiers; restart, speculative/committed agreement, concurrent authority and old exact retries pass. Default placement works for anonymous/nested maps with explicit override. SC-01/SC-02 have source-backed dispositions and regressions, not unsupported parity claims.

### 3. General query data and relations — Goal 4

**Status:** Complete; general typed/EDN/program, PostgreSQL and application acceptance verified.
**Audit ownership:** Q02 (six-plus columns), Q04.

**Outcome:** Applications query ordinary data of arbitrary relation width and pass nil, maps, collections and other supported query-only values without forcing them into stored datom types.

**Focus:** Introduce additive query-only representations and callback contracts while retaining existing typed wrappers and the optimized datom path. Extend EDN adapters and version persisted query/program forms only when new representations are used. Keep equality/hash, shape, resource limits and immutable source identity coherent.

**Completion signal:** Typed and EDN tests cover arbitrary-width named sources, mixed nested general values, constants and database-free callbacks, with prepared reuse, rule/subquery scoping and exact numeric behavior. Old program bytes/receipts remain valid; bounded memory/work and actual PostgreSQL mixed-source workflows pass.

### 4. Native application computation — Goal 5

**Status:** Complete; grouped/portable/native computation and application/PostgreSQL acceptance verified.
**Audit ownership:** Q01, Q05, ST-02.

**Outcome:** Applications use grouped custom aggregates, practical standard data functions and explicitly deployed Rust transaction functions/predicates in ordinary production workflows.

**Focus:** Build grouped aggregate and row-function contracts on the accepted query-value domain. Provide the portable functions actually needed by documented examples; no Clojure interpreter/reflection. Separate trusted process-local registrations from content-addressed persisted programs, with explicit deployment/version identity, db-before/db-after phases and receipt-first retry behavior.

**Completion signal:** Weighted multi-argument aggregation, database-free helpers, native transaction transformations and predicates work through public/EDN interfaces. Speculation and committed execution agree; rebinding/restart never reinterprets retained receipts. Callback cooperation/limits and deployment requirements are documented and tested.

### 5. Safe logical database lifecycle — Goal 6

**Status:** Active; reconciling lifecycle identity, fencing and reclamation boundaries.
**Audit ownership:** P02.

**Outcome:** Users can idempotently create, list, rename, retire/delete and eventually reclaim logical databases through supported APIs and CLI.

**Focus:** Use PostgreSQL coordination and stable database lineage, not ad hoc catalog edits. Define active writers/readers, exact references, retention, backups, rename aliases/name reuse and reclamation separately. Make destructive targets explicit and observable.

**Completion signal:** Isolated real PostgreSQL tests exercise concurrent create, identity-preserving rename, active-service fencing, delete versus reclamation, old reference behavior, backup protection and name reuse. Ordinary users need no hand-written catalog SQL; failures do not partially rename/delete or reuse identity.

### 6. Reliable stock services and clients — Goal 7

**Status:** Planned; scaffolded, not active.
**Audit ownership:** AO04, AO05, AO-C01, P04, AO07; AO-C03 disposition.

**Outcome:** Stock executables support persistent active/standby operation, bounded automatic excision, recoverable routing, usable asynchronous clients and clear health/readiness.

**Focus:** Reuse fenced service/standby/excision mechanisms and existing native transports. Retain configured indexing/listener/TLS settings across takeover. Add Rust Future/stream-facing adapters without blocking executor threads; distinguish dropping a waiter from cancelling an already durable transaction. Define liveness versus write readiness. Evaluate PID/multi-database packaging by actual supervisor needs.

**Completion signal:** Actual separate processes show standby takeover, exact retries through route change, read continuity, committed excision progress under runtime roles, shutdown and failure recovery. Single-thread executor checks prove nonblocking public adapters; health endpoints reflect actual availability without leaking credentials. Packaging differences receive explicit user-oriented dispositions.

### 7. Actionable diagnostics and operational signals — Goal 8

**Status:** Planned; scaffolded, not active.
**Audit ownership:** D01, D02, D03, AO06, Q06.

**Outcome:** Users can explain expensive queries/transactions and operate the product using correlated, named metrics and structured events.

**Focus:** Extend the existing attribution system with per-operation cache/index work, stable clause/binding/phase identities and transaction-correlated semantic counters. Add bounded configurable publication/logging and meaningful alarms. Retain the native optimizer by default; explain actual scheduling rather than copy Clojure tuning rituals.

**Completion signal:** Concurrent cached/uncached reads, nested queries, upserts/program transactions and indexing/failover produce attributable reports without cross-operation contamination or payload/secret leakage. Stock service emits verified events/counters; disabled instrumentation has measured costs and no semantic effects.

### 8. Bounded large-data reads and maintenance — Goal 9

**Status:** Planned; scaffolded, not active.
**Audit ownership:** P05, AO09, ST-04, AO10, AO11.

**Outcome:** Selective offline backup reads, repeated log reads and maintenance use bounded resources with useful, measured operating controls.

**Focus:** Reuse immutable backup/log/index representations and caches. Add lazy backup access without restore/full materialization, authenticated log SSD reuse, and only effective configurable prefetch/index/backup/GC concurrency or pacing. PostgreSQL remains the durable product store; a backup reader is not a second writer backend.

**Completion signal:** Increasing datasets show selective backup reads and restart-hot log reads with measured I/O/memory attribution; corruption/unavailability fail safely. Concurrency/pacing checks demonstrate bounded admission, cancellation and interference under real workloads. No speedup is asserted merely because a setting exists.

### 9. Integrated product acceptance and compatibility — Goal 10

**Status:** Planned; scaffolded, not active.
**Audit ownership:** All required stages; AO-C02.

**Outcome:** The accepted capabilities form one usable native product with preserved data and credible upgrade/operational guidance.

**Focus:** Exercise an evolving end-user scenario through public Rust and EDN interfaces, real PostgreSQL and stock services. Revalidate old receipts/programs/snapshots, restart/failover, deletion/excision safety and measured complete paths. Establish the supported rolling-upgrade envelope from actual version fixtures; do not claim universal mixed-version support.

**Completion signal:** All required child signals and integrated workflows pass without skipped PostgreSQL claims or hidden gaps. Every audit ID has a verified implementation, supported native-equivalent disposition or explicit user decision. Reopen owning children for gaps; never mark this parent complete solely because scaffolds or one child exist.

## Explicit optional and platform dispositions

No audit item disappears merely because it is optional.

| Audit IDs | Disposition |
| --- | --- |
| AO02, AO13 | Optional thin-client gateway / graphical console; technically feasible in native Rust over existing EDN/peer APIs. Await phase-scope choice; no new network service/UI assumed yet. |
| AO08, ENV02 | Optional shared network cache and AWS integrations; feasible, but introduce external operational dependencies. Await scope choice and deployment requirements. PostgreSQL durability would remain authoritative. |
| TU-01 | Existing pure immutable memory engine is supported; a named in-process Connection facade is an optional development workflow, not an absent engine. Keep explicit pending decision; do not redesign every Connection around a new store solely for spelling parity. |
| OC-C1 | SQL/BI connector is a research/scope candidate from release history; the corpus lacks its complete contract. Validate user workflow/spec before implementation. |
| TU-02, ENV01 | Alternate durable stores and vendor topologies are outside the retained native Rust/PostgreSQL objective. Local PostgreSQL development remains supported. |
| ENV03 | JVM/runtime/distribution specifics are not implementation goals. Portable intents belong to the relevant native stages. |
| AO-C03 | Assess supervisor-native process identity and multi-database packaging in Goal 7; do not add PID files merely to copy scripts. |
| Q06, SC-01, SC-02 | Preserve sound native optimizer/numeric/identity behavior unless evidence establishes a semantic defect; provide transparent guidance and regressions. |

A nonblocking question asks whether the optional gateway/console/cache/AWS work
belongs in this phase. Until answered, execute the required native stages above;
do not silently count pending choices as implemented or expand into unrelated
services. If authorized, add ordinary subsequent indexed children here—never a
new corrective parent. Changes to this disposition require explicit evidence or
user direction, not silent scope reduction.

## Parent loop and completion

Reconcile the active child with code/tests, execute its first unfinished internal
stage, then fold decisions/results here and into its plan. On its full completion,
return here and activate the next unfinished child. Reopen the owning child when
integration exposes a gap. A scaffold or one completed child is not the finish line.

Parent completion requires all required native capabilities and integrated
acceptance plus an explicit disposition for every audit ID. Unknowns remain
unknown; optional decisions are not fake passes. No universal Datomic/JVM parity
claim is implied.

Verified Goal 2: mixed-source sequences (also bound EDN), physical AVET readiness,
partial tuple authoring, entity Eq/Hash/identity and direct captured-function invocation.
Hidden-row/lookup cancellation and legacy eager-origin gaps were reproduced and fixed.
Its plan records 127 optimized public/integrated passes (one opt-in benchmark ignored),
actual restricted-role applications/restarts, unchanged old receipts and sampled costs.
No canonical codecs or migrations changed. Clippy retains 24 pre-existing warnings.

Verified Goal 3: independent reserved allocation with versioned ATLC v2/migration
31, explicit execution-default placement and source-backed composite/NaN
dispositions. Pure and actual PostgreSQL million-ID histories pass; the retained
old history's 65 canonical transactions and 1,047,577 receipt IDs are unchanged.
Genuine old program/partition/identity fixtures and the application pass. Legacy
generation-zero conversion now preserves exact receipts using authenticated,
incrementally built historical indexes; its 515-update interruption/GC/corruption/
consolidation/restart workflow passed. The final malformed-content/receipt guards
have separate final-build regressions; Goal 3 records artifact boundaries and costs.

Verified Goal 4: additive general query values/relations, pure callbacks,
source-free and mixed EDN application workflows; preserved old native output
shapes/ABI bytes/receipts. Template 3/ABI 10 covers new representations. Schema32
rebuilds complete native-query program references with quiesced upgrade/startup
fencing and no canonical rewrite. Final configured application/query and old
program/GC/restart checks pass; measured complete general queries include N log N
projection costs. Its plan records exact counts, artifacts and admission limits.

Verified Goal 5: borrowed grouped native aggregates, seven practical portable data
functions and explicit versioned Rust transaction/predicate deployments. New query
operators use template4/ABI11; migration33 fences old startups without rewriting
canonical data. Final configured release tests passed 36/36, zero skipped/ignored,
including compiled-host/stock-EDN clients, genuine schema32 upgrade, exact retries,
speculation, backup/restore, durable invocation and the evolving application.
Ignored callback budget failures are sticky; predicate-marker checks do not block
unrelated legacy data. Goal 5 records pure regressions, measured full paths and
cooperative callback/deployment limits.

Continuation: execute Goal 6's safe logical database lifecycle. Goals 1–5 are complete;
the parent and later stages are not. Preserve Goal 3's retained fixture identities
and compatibility evidence rather than reseeding them for later checks.
Optional deployment/UI scope is awaiting user preference, not blocking native work.
