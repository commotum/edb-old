# Goal 0 — Native Datomic-Inspired Database

## Objective

Build a production-quality database in Rust that preserves the central benefits and stated semantics described by `datomic_pro_docs`: immutable datoms and database values, declarative serialized transactions, durable history and time views, local peer query, useful ordered indexes, strong identity, and operationally sound durability. Reconstruct `1.0.7705` as faithfully as practical at the level of architecture, data flow, algorithms, invariants, and performance choices, translating it idiomatically into Rust rather than inheriting its JVM or Clojure runtime.

PostgreSQL is the only storage system this project will support. The design should use PostgreSQL directly and well; portability to other storage backends is not an objective.

## Constraints

- Implement the system and its public/runtime-facing components in Rust; do not require a JVM or Clojure.
- Treat `datomic_pro_docs` as the semantic authority and `1.0.7705` as the default architectural and algorithmic blueprint. Study its namespaces, class and type boundaries, data representations, control flow, caches, concurrency choices, and storage/index/query algorithms for both intent and performance implications.
- Preserve recognizable implementation structure and behavior from `1.0.7705` wherever it remains sound in Rust and PostgreSQL. Deviate deliberately only when required by documented semantics or when a direct translation would add JVM/Clojure-specific machinery, obscure safety, or be materially less idiomatic or efficient in Rust.
- Record significant deviations and their evidence. Resolve conflicts in favor of the documented model unless evidence justifies an explicit project decision.
- Do not require compatibility with, or the ability to open, existing Datomic databases.
- Support PostgreSQL only. Do not create a generic storage backend layer or spend effort preserving hypothetical backend portability.
- Preserve the information model, observable guarantees, and useful internal design insight. Java/Clojure names, types, protocols, and serialized shapes are evidence to study and map, even when the final Rust API or byte encoding is not compatibility-identical.
- Keep the authoritative write path small, deterministic, serialized, recoverable, and testable.
- Keep query work and immutable-data caching at peers so reads can scale independently of the transactor.
- Prefer explicit, versioned native formats and deterministic sandboxing for persisted behavior.

## Architectural destination

- **Value and datom model:** canonical native types, entity and transaction identifiers, total value ordering, immutable datoms, schema-as-data, and versioned encoding.
- **PostgreSQL storage:** a concrete schema for immutable blocks, the transaction log, database roots, leader epochs, idempotency records, indexing progress, backup metadata, and atomic publication through PostgreSQL transactions and conditional updates.
- **Transactor:** a deterministic state machine that resolves identity, expands transaction data and functions, validates the complete proposed information set, derives datoms and index roots, commits once, and emits transaction reports.
- **Indexes:** EAVT, AEVT, AVET, VAET, history, and a recent transaction layer represented by immutable persistent structures with background consolidation.
- **Peer:** immutable database snapshots, PostgreSQL-backed synchronization, permanent-safe caching of immutable data, local index access, local query and pull, and transaction submission.
- **Query and pull:** native Datalog evaluation with set semantics, joins, rules, recursion, negation, aggregates, predicates, time views, and graph projection.
- **Function runtime:** built-ins plus deterministic, resource-bounded persisted behavior through a versioned WASM ABI or comparably constrained DSL; no ambient filesystem, network, clock, or randomness.
- **Operations:** fenced transactor failover, restart recovery, backups, restore verification, observability, garbage collection, excision, schema/index evolution, and structured errors.

## Execution model

Goal 0 is the persistent parent loop, not a direct implementation stage. For each first unfinished numbered stage, it owns this cycle:

1. Reconcile the parent plan and repository with actual artifacts, tests, source evidence, and completed child-goal findings.
2. Create the matching `goal-N/` scaffold with `$scaffold-goal` if it does not exist, or reconcile and resume it if it does. A child scaffold contains `0-plan.md`, `0-loop.md`, and `0-prompt.md` and inherits this objective and all applicable constraints.
3. Execute that child goal through its observable completion signal. Creating its scaffold, writing a plan, or completing only an internal stage does not complete the parent stage.
4. Fold material evidence, decisions, deviations, limitations, and truthful status into both the child plan and this plan.
5. Return to Goal 0, reread the whole parent plan, and revise unfinished stages when new evidence changes their best boundaries or acceptance criteria; preserve the original objective and do not renumber completed stages.
6. Repeat with the new first unfinished stage until every stage and the Goal 0 success condition are verified.

Only one child goal is active at a time. If its scaffold already exists but is incomplete, continue it rather than replacing it or creating the following scaffold. If blocked, record the concrete blocker in both plans and stop truthfully instead of advancing the parent stage.

## Stages

### 1. Semantic foundation

**Status:** Complete via `goal-1/`. The semantic contract, pure Rust reference transition, structured errors, and conformance suite establish the acceptance boundary for Stage 2.

**Outcome:** A precise, testable native contract for values, datoms, schema, identity, transactions, database views, errors, and ordering, including explicit decisions for behavior the source material leaves ambiguous.

**Focus:** Extract normative behavior from `datomic_pro_docs`; map the corresponding types, namespaces, data flow, algorithms, and edge cases in `1.0.7705`; define canonical Rust-facing semantics, encodings, invariants, and conformance fixtures; document where an idiomatic Rust translation preserves or intentionally changes the recovered structure.

**Completion signal:** The core semantic specification is internally consistent, disputed cases have recorded decisions, and executable fixtures or a minimal reference model can distinguish conforming from nonconforming behavior.

### 2. Single-process transactional kernel

**Status:** Complete via `goal-2/`. The Rust kernel now provides immutable indexed database values, the declarative transaction pipeline, schema/identity evolution, typed map and native function expansion, temporal/filtered reads, and invariant/permutation verification. PostgreSQL remains absent as required by this stage.

**Outcome:** A correct in-memory database kernel can apply complete unordered transactions to immutable database values.

**Focus:** Datoms and indexes; transaction expansion; tempids and lookup refs; uniqueness and upsert; cardinality; CAS and retractions; schema enforcement; transaction functions; history; and pure `with` behavior.

**Completion signal:** The kernel passes the semantic conformance corpus, property tests preserve its invariants, and the same input database and transaction always produce the same result.

### 3. PostgreSQL durability and recovery

**Status:** Complete via `goal-3/`. The Rust kernel now has a versioned canonical format, one constraint-backed PostgreSQL schema, immutable chained history, expected-basis/hash publication, durable idempotency, fail-closed recovery, a verified current-value write cache, and real PostgreSQL 15.11 restart/concurrency/process-death/corruption evidence. Checkpoints were deliberately not introduced; the authoritative log is sufficient until Goal 4 owns persistent read indexes.

**Outcome:** The kernel commits durable database history exclusively through PostgreSQL and reconstructs correct state after clean or abrupt restart.

**Focus:** Concrete SQL schema and migrations; immutable records or blocks; transaction log; root publication; checksums; transaction idempotency; isolation and locking choices; atomic conditional commit; indexing checkpoints; corruption detection; and recovery.

**Completion signal:** Acknowledged commits survive injected crashes, ambiguous retries cannot double-commit, failed publications are invisible, and recovery reproduces the expected basis and indexes.

### 4. Index and peer read architecture

**Status:** Complete via `goal-4/`. PostgreSQL now stores immutable canonical
content-addressed index leaves and transaction-anchored manifests;
deterministic consolidation builds all current/history index orders over a
base plus contiguous recent tail. Independent peers recover from verified
roots, synchronize monotonically, retain old `Arc<Database>` snapshots, expose
local temporal/index access and transaction reports, cache immutable segments,
and fall back to the authoritative log on derived corruption. Concurrent
builders/readers, interrupted publication, `noHistory`, multiple bases, cache
pressure, and an actual PostgreSQL 15.11 restart are verified.

**Outcome:** Independent Rust peers maintain immutable database snapshots and answer indexed reads locally while synchronizing monotonically through PostgreSQL.

**Focus:** EAVT, AEVT, AVET, VAET and history access; persistent and recent index layers; immutable segment caching; snapshot lifecycle; basis synchronization; `as-of`, `since`, and history views; transaction reports; and background consolidation.

**Completion signal:** Multiple peers can lag, synchronize to requested transactions without gaps, retain valid old snapshots, and return identical indexed results across restart and consolidation.

### 5. Query, pull, and native API

**Status:** Complete via `goal-5/`. The typed Rust query engine evaluates local
immutable snapshots with relation/set semantics, every input/find shape,
binding-aware index planning plus a full-scan oracle, logical joins, bounded
fixed-point rules, predicates/functions, deterministic aggregates and `with`,
native plan/stats, cancellation and resource limits. Pull/entity navigation
supports wildcard, reverse refs, components, recursion/cycles, defaults,
aliases and limits. The peer API composes expected-basis/idempotent transact,
sync, query, pull and entities; PostgreSQL 15.11 and concurrent old-snapshot
tests verify the integrated boundary. `goal-5/SUPPORTED_SURFACE.md` names
intentional JVM host-language and peripheral omissions.

**Outcome:** Applications can use an ergonomic Rust API to transact, navigate entities, pull graphs, and run the documented Datalog model locally at peers.

**Focus:** Query inputs and relations; joins; rules and fixed-point recursion; negation and disjunction; predicates and functions; aggregates; bag behavior around `with`; pull recursion and components; cancellation; limits; and explainable execution.

**Completion signal:** The supported query and pull surface passes semantic fixtures and differential tests against the simple reference evaluator, with no promised result ordering unless explicitly requested.

### 6. Controlled programmability

**Status:** Complete via `goal-6/`. Versioned canonical straight-line bytecode
provides metered transaction functions, attribute predicates, and query
extensions with no ambient authority. Immutable PostgreSQL deployment,
conditional activation, exact-hash caching/idempotency, same-db-before batch
composition, kernel revalidation, local Rust extension separation, restart and
corruption behavior, and byte-identical independent execution are covered by
focused tests and the 87-pass integrated real-PostgreSQL suite. The deliberately
smaller-than-Clojure surface and threat model are explicit in the child docs.

**Outcome:** Custom transaction and query behavior is expressive enough for application invariants without compromising determinism or transactor safety.

**Focus:** Native built-ins; process-local Rust query extensions; sandboxed persisted functions; db-before and db-after evaluation rules; versioned ABI; resource metering; cancellation; deployment; and reproducibility.

**Completion signal:** Persisted functions execute identically across nodes and restarts, reject forbidden effects, obey resource limits, and cannot bypass transaction validation.

### 7. Availability and production transaction service

**Status:** Active via `goal-7/`; scaffolded after verified Goal 6 completion.

**Outcome:** The serialized write service remains correct through concurrency, overload, timeout, process failure, and leader replacement.

**Focus:** Request queues; backpressure; structured anomalies; unknown outcomes; durable idempotency keys; transaction reports; leader election; monotonically increasing epochs; fencing at publication; standby catch-up; and failure injection.

**Completion signal:** At most one leader epoch can publish, failover never forks history, retry behavior is explicit, and stress/fault tests preserve serializable outcomes.

### 8. Lifecycle and operational completion

**Outcome:** The system can be operated, protected, upgraded, diagnosed, and retired responsibly in production.

**Focus:** Backup and point-in-time recovery; restore verification; metrics and tracing; consistency inspection; capacity controls; garbage collection; privacy-oriented excision and cache invalidation; migrations; compatibility policy; security; and operator documentation.

**Completion signal:** Rehearsed operational procedures meet declared recovery and privacy guarantees, long-running workloads remain bounded and observable, and the full acceptance suite passes in a production-shaped deployment.

## Success condition

Goal 0 is complete when the Rust transactor and peers, backed only by PostgreSQL, deliver the documented core benefits as one coherent, tested, recoverable system; the major guarantees hold under concurrency and injected failure; and remaining differences from Datomic are intentional and documented rather than accidental.
