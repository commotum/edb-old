# Goal 3 — PostgreSQL Durability and Recovery

## Objective

Make the completed single-process Rust kernel durable exclusively through PostgreSQL. A successful transaction must become visible as one atomic, ordered publication; acknowledged commits must survive restart; ambiguous retries must not create duplicate transactions; and recovery must reconstruct the same basis, schema, history, and in-memory indexes that the kernel produced before shutdown.

This goal realizes Stage 3 of `goal-0/0-plan.md`. It establishes the durable write and recovery boundary that later transactor services and peers will use, without implementing peer synchronization, background index consolidation, leader failover, or the production transaction service.

## Constraints

- Keep all runtime code in Rust and use PostgreSQL directly. Do not introduce a generic storage abstraction or support another database.
- Treat `datomic_pro_docs` as the semantic authority, Goal 1 and Goal 2 as the executable kernel contract, and `1.0.7705` as the default implementation blueprint for immutable values, transaction logs, roots, conditional publication, SQL behavior, and recovery.
- Trace the relevant recovered `datomic.log`, `datomic.kv-store`, `datomic.kv-sql`, `datomic.sql`, catalog, and database-root paths before replacing their design. Preserve useful boundaries and algorithms where they remain sound in PostgreSQL and idiomatic Rust.
- Preserve `Database::with` as a pure transition. Durable orchestration supplies db-before and transaction context, then persists the returned successor; SQL concerns must not leak into semantic assessment.
- A database has one gap-free, monotonically increasing committed basis. No reader or restart may observe an unpublished or partially published transaction.
- Await PostgreSQL commit acknowledgment before reporting success. Represent a lost or indeterminate acknowledgment as an explicit unknown outcome, resolvable by idempotency key.
- Use explicit, versioned native encodings with canonical bytes and checksums. Do not use Java serialization, Fressian compatibility, PostgreSQL collation, Rust enum discriminants, or an unstable serializer as persistent meaning.
- Test real PostgreSQL transaction, locking, constraint, and crash behavior. Mocks may support unit tests but cannot establish the completion signal.
- Keep peer caches/synchronization, query and pull, durable segmented indexes, background consolidation, service queues/backpressure, leader election/fencing, backup operations, and multi-node availability in their later Goal 0 stages.
- Do not claim crash safety from happy-path restart tests alone. Exercise kill points and ambiguous retry boundaries around publication.

## Known context

- Goal 2 provides a deterministic pure kernel, immutable database values, transaction-grouped datoms and schema changes, current/history EAVT/AEVT/AVET/VAET roots, structured errors, and `Database::validate_invariants`.
- Goal 2 deliberately leaves persistent schema identifiers and bytes undefined. This goal must fix the versioned encoding for every durable value, datom, schema change, transaction envelope, and root/checkpoint reference it stores.
- The documented ACID model writes immutable tree/log values and conditionally updates a small durable root reference. In PostgreSQL, immutable inserts plus one conditional head update inside a SQL transaction are the closest direct native equivalent.
- `1.0.7705` separates logical transaction processing from `datomic.log` and the `kv-store`/`kv-sql` persistence boundary. The Rust design should retain that separation without recreating storage portability protocols.
- The transaction log is authoritative for this milestone. Checkpoints may bound recovery work, but must be derived, validated, and safely discardable; Goal 4 owns production persistent index layers and peer-local read architecture.
- Idempotency is part of the durable commit contract here even though request queues and the network-facing transaction service arrive later. A retry needs a stable key and request digest before those layers exist.

## Stages

### 1. Durable contract and recovered write spine

**Outcome:** The exact durable invariants and PostgreSQL transaction boundary are settled before bytes or tables become commitments.

**Focus:** Map documented atomicity, isolation, durability, total basis order, log visibility, and unknown outcomes to the relevant `1.0.7705` log/root/SQL control flow; define authoritative versus derived state; define commit states and retry outcomes; decide the PostgreSQL isolation/locking strategy and expected-basis conditional publication rule.

**Completion signal:** A concise contract names every durable record, invariant, commit point, recovery authority, and failure outcome, with significant deviations from the recovered design justified by PostgreSQL or Rust rather than convenience.

### 2. Canonical native encoding

**Outcome:** Every logical value needed for durable replay has stable, versioned, corruption-detectable bytes independent of process/compiler/database behavior.

**Focus:** Encode values, entity and transaction IDs, datoms, schema descriptors and changes, transaction context/report essentials, transaction envelopes, chain links, and checkpoint/root metadata; preserve `Value::index_cmp` and strict stored equality; define length/bounds validation, version dispatch, canonical map/set order, checksums, and unsupported-version behavior.

**Completion signal:** Golden vectors and round-trip/property tests cover all value variants and edge cases; semantically identical inputs produce identical bytes; malformed, truncated, noncanonical, oversized, unsupported, and checksum-invalid payloads fail with stable structured errors.

### 3. Concrete PostgreSQL schema and migrations

**Outcome:** A fresh PostgreSQL database can be migrated to one explicit storage layout that enforces the durable model instead of relying on application convention alone.

**Focus:** Database catalog/identity; immutable transaction payloads or objects; per-database ordered transaction records and predecessor links; the single published head; schema/checkpoint metadata; idempotency key plus request digest and outcome; checksums, foreign keys, uniqueness, immutability protections, migration versioning, roles, and least-required privileges.

**Completion signal:** Migrations are repeatable from empty storage, schema constraints reject malformed chains and duplicate publications, ordinary runtime credentials cannot mutate immutable committed history, and integration tests inspect the actual PostgreSQL catalog and constraints.

### 4. Atomic commit and idempotent retry

**Outcome:** The kernel successor and its durable transaction envelope are published exactly once at the expected basis or not published at all.

**Focus:** Load and verify the current head; run the pure kernel against that exact db-before; write immutable payloads; record the idempotency key/request digest; conditionally advance the head from expected basis to successor basis in one PostgreSQL transaction; handle concurrent writers, serialization failures, duplicate matching retries, duplicate mismatched retries, commit acknowledgment loss, and structured rejected/known/unknown outcomes.

**Completion signal:** Concurrent attempts cannot fork or skip history; the same key and request returns the original committed outcome; key reuse with different input is rejected; rollback leaves no visible transaction; and success is returned only after PostgreSQL acknowledges commit.

### 5. Restart recovery and checkpoint discipline

**Outcome:** Clean or abrupt startup reconstructs the latest valid immutable `Database` and detects damage instead of silently accepting it.

**Focus:** Read the published head; validate database identity, encoding versions, checksums, predecessor chain, contiguous basis, transaction identity, and schema-change history; replay through the Goal 2 kernel representation; verify recovered roots and invariants; introduce only a derived atomic checkpoint mechanism needed to bound replay; ignore or diagnose unreachable writes without making them visible.

**Completion signal:** Recovery from log alone and from each accepted checkpoint yields the same basis, schema, history, and all four current/history index orders as the pre-restart kernel; missing/corrupt/reordered reachable records fail closed; unreachable partial objects never enter the recovered database.

### 6. Crash, concurrency, and durability verification

**Outcome:** The PostgreSQL boundary has evidence for its guarantees under the failures that make durability difficult.

**Focus:** Deterministic kill points before payload write, before head update, before SQL commit, and after commit before response; process restart; connection loss and timeout around commit; duplicate retries; competing writers; migration races; corrupted payload/checksum/version/chain cases; long replay and checkpoint equivalence; and continued execution of the Goal 1/2 conformance suites.

**Completion signal:** A real-PostgreSQL fault matrix demonstrates that acknowledged commits always recover, failed publications are invisible, every ambiguous retry resolves without double commit, history remains gap-free, corruption is detected, recovered indexes match the original, and all Rust tests and lints pass without warnings.

## Durability exit condition

Goal 3 is complete when the Rust kernel can create and reopen a PostgreSQL-backed database, publish transactions through one conditional atomic head advance, resolve retries by durable idempotency identity, and recover exactly the last committed database value after clean shutdown, process death, or ambiguous acknowledgment. The result must be demonstrated against real PostgreSQL with fault injection and must leave peer synchronization, durable read-index architecture, HA leadership, and production service orchestration clearly unclaimed.

