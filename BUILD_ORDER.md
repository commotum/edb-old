# EDB — Recommended Build Order

Summary
- Build bottom‑up. Establish canonical encodings and ordering; validate transactions; persist an append‑only log; add schema/identity; enforce uniqueness; add indexes (EAVT → AVET/VAET); layer query and pull; then APIs, observability, P2P, and CLI/SDKs.

Steps
1. Core Value Encoding + Datom Types
   - Deliver: Order‑preserving `encode(value)->bytes`, `decode(bytes)->value`; tuple v1 (homogeneous; header not in compare; lexicographic slots).
   - Tests: Golden vectors, order laws, round‑trips.
2. Transaction Model + Validation Engine
   - Deliver: Grammar normalization, tempids, lookup refs, type/cardinality/ref checks, tx‑fn harness; tx report.
   - Tests: Positive/negative tx fixtures; deterministic normalization.
3. Append‑Only Log + t Assignment (SQLite first)
   - Deliver: Crash‑safe append, monotonic t, CAS root update, replay; tx‑report bus.
4. Schema Catalog + Identity/Lookup + Components
   - Deliver: Attributes as data; idents/enums; lookup refs; components + retractEntity; aliases.
5. Unique Enforcement (identity/value)
   - Deliver: Upsert on identity, reject on value; current (a,v)->e map.
6. Live Memory Index + Background Indexer (EAVT)
   - Deliver: In‑memory delta + durable segment trees; merge and adoption.
7. AVET + VAET Indexes
   - Deliver: Value lookup/ranges; reverse edges.
8. Query Engine (parse → algebrize → plan) + Built‑ins
   - Deliver: Datalog subset; plan pushes ranges to AVET; joins via EAVT/AEVT.
9. Pull Engine (patterns, reverse, options)
   - Deliver: Pattern normalization/cache; reverse attrs; options; recursion limits.
10. API Server (HTTP/gRPC)
   - Deliver: tx/db/q/pull/sync(t)/subscribe; minimal auth.
11. Observability
   - Deliver: metrics, logs, tx‑reports.
12. P2P Sync MVP
   - Deliver: Heads, push/pull, signatures, snapshots; feature flags.
13. CLI/SDKs
   - Deliver: CLI and thin language wrappers.

Why This Order (plain language)
- You need stable bytes to compare and hash before you can write or index. A durable log and validation precede “having a database.” Indexes make queries fast; query and pull power apps. APIs make it usable; observability makes it operable. P2P comes last, after single‑node correctness and visibility.

References
- `overview.md:1`
- `docs/research/EDB-REQUIREMENTS.md:1`
- `docs/research/value-types.md:1`
- `docs/research/tuple-encoding.md:1`
- `docs/research/indexing-strategy.md:1`
- `datomic-reference/*` (read‑only ground truth)

## Per‑Step Reference Map (Datomic)

1. Core Value Encoding + Datom Types — see `edb/01-encoding/README.md:1`
   - Primary: `datomic-reference/programming_with_data_and_edn.md:1`, `datomic-reference/entities.md:1`
   - Helpful: `datomic-reference/overview.md:1`

2. Transaction Model + Validation — see `edb/02-tx-model/README.md:1`
   - Primary: `datomic-reference/transactions/2_transaction_model.md:1`, `datomic-reference/transactions/3_transaction_data.md:1`
   - Helpful: `datomic-reference/transactions/4_processing_transactions.md:1`, `datomic-reference/transactions/6_acid.md:1`

3. Append‑Only Log + t (SQLite) — see `edb/03-log/README.md:1`
   - Primary: `datomic-reference/operation/2_transactor.md:1`, `datomic-reference/transactions/4_processing_transactions.md:1`
   - Helpful: `datomic-reference/operation/1_storage.md:1`

4. Schema Catalog + Identity/Lookup + Components — see `edb/04-schema/README.md:1`
   - Primary: `datomic-reference/schema/1_schema.md:1`
   - Helpful: `datomic-reference/schema/2_changing_schema.md:1`, `datomic-reference/schema/3_data_modeling.md:1`, `datomic-reference/entities.md:1`

5. Unique Enforcement (identity/value) — see `edb/05-unique/README.md:1`
   - Primary: `datomic-reference/schema/4_identity_and_uniqueness.md:1`
   - Helpful: `datomic-reference/schema/1_schema.md:1`

6. Memory Index + Background Indexer (EAVT) — see `edb/06-index-eavt/README.md:1`
   - Primary: `datomic-reference/indexes/2_index_model.md:1`
   - Helpful: `datomic-reference/indexes/3_background_indexing.md:1`, `datomic-reference/overview.md:1`

7. AVET + VAET Indexes — see `edb/07-index-avet-vaet/README.md:1`
   - Primary: `datomic-reference/indexes/2_index_model.md:1`
   - Helpful: `datomic-reference/indexes/3_background_indexing.md:1`, `datomic-reference/overview.md:1`

8. Query Engine (parse → algebrize → plan) — see `edb/08-query/README.md:1`
   - Primary: `datomic-reference/query_and_pull/3_query.md:1`, `datomic-reference/query_and_pull/2_executing_queries.md:1`
   - Helpful: `datomic-reference/technicals/outer_joins.md:1`

9. Pull Engine — see `edb/09-pull/README.md:1`
   - Primary: `datomic-reference/query_and_pull/4_pull.md:1`, `datomic-reference/time_in_datomic.md:1`

10. API Server (HTTP/gRPC) — see `edb/10-api/README.md:1`
   - Primary: `datomic-reference/operation/tutorial/2_api.md:1`
   - Helpful: `datomic-reference/operation/tutorial/4_read.md:1`, `datomic-reference/operation/tutorial/3_assertion.md:1`

11. Observability — see `edb/11-observability/README.md:1`
   - Primary: `datomic-reference/transactions/4_processing_transactions.md:1`
   - Helpful: `datomic-reference/operation/2_transactor.md:1`, `datomic-reference/technicals/write_a_problem_report.md:1`

12. P2P Sync MVP — see `edb/12-p2p/README.md:1`
   - Helpful: `datomic-reference/overview.md:1`

13. CLI/SDKs — see `edb/13-cli/README.md:1`
   - Primary: `datomic-reference/operation/tutorial/2_api.md:1`
   - Helpful: `datomic-reference/programming_with_data_and_edn.md:1`, `datomic-reference/best_practices.md:1`

## Status (MVP tracking)

- [x] 1. Core Value Encoding + Datom Types
  - Implemented in Python and Rust (edb-encoding). Includes bigint, decimal, float32/16/bfloat16; tuple v1 (homogeneous) with both fixed‑width and variable‑length slot support (BYTES excluded in v1). Golden vectors generated and consumed by Rust tests. CLI `edb-enc` available for ad‑hoc encoding.
- [x] 2. Transaction Model + Validation Engine
  - Grammar normalization (list form), tempids, lookup refs, type/cardinality/ref checks, uniqueness (identity/value), cardinality‑one implicit retracts, tx‑fn harness stub. Deterministic normalization and unit tests added.
- [~] 3. Append‑Only Log + t Assignment (SQLite)
  - Implemented SQLite store (segments/roots/log) with CAS root support; minimal transactor appends tx to log (monotonic t via rowid). Replay and tx‑report bus are pending; roots CAS integrated at store layer but not adopted by transactor yet.
- [~] 4. Schema Catalog + Identity/Lookup + Components
  - Attribute catalog persisted; identity/lookup enforced in validation and DbView. Component cascade retract and aliases not implemented yet (planned).
- [x] 5. Unique Enforcement (identity/value)
  - Enforced in validation; unique/identity upsert; unique/value conflict detection; mock and transactor tests.
- [ ] 6. Memory Index + Background Indexer (EAVT)
  - Next: implement memory delta + durable segment trees; merge + CAS adoption.
- [ ] 7. AVET + VAET Indexes
- [ ] 8. Query Engine (parse → algebrize → plan)
- [ ] 9. Pull Engine (patterns, reverse, options)
- [ ] 10. API Server (HTTP/gRPC)
- [ ] 11. Observability
- [ ] 12. P2P Sync MVP
- [ ] 13. CLI/SDKs

Notes
- We intentionally borrowed from Datomic’s K/V + CAS pattern so both SQLite and Postgres backends can share the same abstraction. Postgres can be plugged later without changing transactor/index code.
- For tuples, v1 excludes BYTES due to lack of scalar length prefix; bytes‑fixed‑N types are recommended for tuple slots.
