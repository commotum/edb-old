# EDB — Recommended Build Order

Summary
- Build bottom‑up. Establish canonical encodings and ordering; validate transactions; persist an append‑only log; add schema/identity; enforce uniqueness; add indexes (EAVT → AVET/VAET); layer query and pull; then APIs, observability, P2P, and CLI/SDKs.

Status (current)
- Completed: 1) Encoding, 2) Tx Model + Validation, 3) Append‑only Log + t, 4) Schema + Identity/Lookup + Components, 5) Unique Enforcement, 6) EAVT (memory + segments, basic scans).
- Improvements applied: EAVT sorts A by normalized content bytes (not length‑prefixed) and encodes V using the schema‑declared value type; Bytes value type cannot be unique/used for lookup.
- Next up: 6a) Transaction enhancements (CAS, tx entity/meta/txInstant, tx‑functions, map‑form tx input, time‑travel view constructors), then 7) AVET + VAET, followed by 8–13.

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
6a. Transaction Enhancements (Datomic parity)
   - Deliver: Compare‑and‑swap (CAS) op; reified transaction entity and `txInstant` (with monotonic override on import); deterministic tx‑functions with registry; map‑form tx input sugar; expose `as‑of/since/history` view constructors.
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

6a. Transaction Enhancements — see `edb/02-tx-model/README.md:1`
   - Primary: `datomic-reference/transactions/4_processing_transactions.md:1`, `datomic-reference/transactions/5_transaction_functions.md:1`, `datomic-reference/time_in_datomic.md:1`
   - Helpful: `datomic-reference/transactions/2_transaction_model.md:1`, `datomic-reference/transactions/3_transaction_data.md:1`

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

## Current Implementation Summary

- Encoding (Step 1)
  - Rust crate `edb/rs/edb-encoding` with canonical, order‑preserving encoders/decoders for scalars and tuple v1.
  - Python spec runner + golden vectors in `edb/01-encoding/tests/vectors` for cross‑runtime conformance.
  - CLI `edb-enc` for ad‑hoc encode/decode and tuple inspection.
- Tx model + validation (Step 2)
  - Rust crates `edb/rs/edb-schema` and `edb/rs/edb-tx` for schema catalog, grammar normalization, tempids, lookup refs, type/cardinality/ref checks, uniqueness, cardinality‑one implicit retract, and tx‑fn harness stub.
  - Unit tests under `edb/rs/edb-tx/tests` cover positive/negative fixtures.
- Append‑only log + t (Step 3)
  - Storage `edb/rs/edb-store-sqlite` (segments/roots/log) with CAS root updates.
  - Transactor `edb/rs/edb-transactor` applies txs atomically, appends to log (monotonic t), updates `head` root, supports subscribe() and replay_current_from_log().
  - Smoke test `edb/rs/edb-transactor/tests/smoke.rs` exercises apply→subscribe→replay.
- Schema extras (Step 4 essentials)
  - Aliases table with resolution in DbView.get_attr; grammar supports `retract-entity` with component cascade.
- Indexing groundwork (Step 6 start)
  - EAVT indexer `edb/rs/edb-index`: builds sorted segment(s) (E asc, A asc, V asc, T desc) and sets `eavt` root. Integrates with transactor (apply→merge for demo); scan_entity(e) performs per‑segment binary search.

## What’s Left (MVP)

- Step 6 (EAVT)
  - DONE (MVP): in‑memory + segment merge; entity scans.
  - Refinements: multi‑segment root/adoption policy; compaction; EA/EAV seek helpers; switch snapshot reads to index where appropriate.
- Step 6a (Transaction Enhancements)
  - Add CAS op to grammar and atomic enforcement in transactor, with structured conflict anomaly.
  - Reify tx entity (tx_eid) and `txInstant`; support monotonic override on import; include tx‑meta in tx‑reports.
  - Wire deterministic tx‑functions via registry; splice expanded ops during normalization.
  - Add map‑form tx input sugar (`{"db/id": ..., ":attr/x": v, ...}`) with lookup/tempid handling.
  - Add `as‑of/since/history` view constructors leveraging t and log.
- Step 7
  - Implement AVET (attrs `:db/index` or unique) and VAET (refs) as sorted segments + scans.
- Step 8–9
  - Query engine (algebrize→plan) with AVET range pushdown; joins via EAVT/AEVT. Pull engine (pattern normalization/cache, reverse, options, recursion limits).
- Step 10–13
  - API server (tx/db/q/pull/sync/subscribe), observability, P2P MVP, CLI/SDKs.

## Build & Test Instructions (for the next model)

- Prerequisites
  - Rust toolchain via `rustup` (stable); ensure `cargo` and `rustc` available.
  - Python 3.10+ (for vector generation) if regenerating vectors.

- One‑time setup
  - From repo root, create workspace: `Cargo.toml` already lists members.
  - (Optional) Regenerate vectors: `PYTHONPATH=edb/01-encoding/src python3 edb/01-encoding/tests/generate_vectors.py`

- Build and run tests
  - `cargo test -q` (from repo root)
  - Crates and tests:
    - `edb-encoding` (lib + tests/vectors.rs)
    - `edb-schema` (lib)
    - `edb-tx` (lib + tests/tx_basic.rs)
    - `edb-store-sqlite` (lib)
    - `edb-transactor` (lib + tests/smoke.rs)
    - `edb-index` (lib)

- Known compile errors and quick fixes
  - edb/rs/edb-encoding/src/scalar.rs
    - `String is a variant, not a module` at string decode (bytes→string)
      - Cause: bare `String::from_utf8_lossy` conflicts with enum variant names; qualify.
      - Fix: use `std::string::String::from_utf8_lossy(bytes).to_string()` or add `use std::string::String;` at top.
      - File: edb/rs/edb-encoding/src/scalar.rs:276
    - `Uuid is a variant, not a module` inside `encode_scalar` UUID arm
      - Cause: `Uuid` variant from `ValueType` matches shadows `uuid::Uuid` type.
      - Fix: fully qualify: `uuid::Uuid::parse_str(s)` or rename import: `use uuid::Uuid as UuidType;` then `UuidType::parse_str`.
      - File: edb/rs/edb-encoding/src/scalar.rs:250
    - BigInt/BigDecimal `is_zero` not found
      - Cause: trait not in scope.
      - Fix: add `use num_traits::Zero;` at top to bring `is_zero()` into scope for `num_bigint::BigInt` and `bigdecimal::BigDecimal`.
      - Files: edb/rs/edb-encoding/src/scalar.rs:386, 484
    - BigInt magnitude bytes
      - Bug: `n.magnitude().to_bytes_be().1` is wrong; `magnitude().to_bytes_be()` returns `Vec<u8>`.
      - Fix: use `let (_sign, mag_bytes) = n.to_bytes_be();` and operate on `mag_bytes`.
      - File: edb/rs/edb-encoding/src/scalar.rs:389
    - BigDecimal normalization parenthesis warning (optional tidy)
      - Suggested: `BigDecimal::new(coeff * pow, 0)` (remove extra parentheses)
      - File: edb/rs/edb-encoding/src/scalar.rs:562

- Optional sanity checks
  - Run CLI: `cargo run -q -p edb-encoding --bin edb-enc -- encode DOUBLE 1.5`
  - Run transactor smoke test only: `cargo test -q -p edb-transactor smoke`

## Handoff Checklist (for the next model)

- Fix compile errors flagged in `edb-encoding/src/scalar.rs` as outlined above.
- Verify `cargo test -q` passes for all crates.
- EAVT refinement:
  - Add EA and EAV scan helpers (seek by (e,a) and (e,a,vPrefix)).
  - Add simple unit tests for comparator and lower_bound behavior with mixed value types.
- Transactor + index integration:
  - Replace current/unique_idx reads with EAVT/AVET/VAET for snapshot reads once indexes are robust; keep current for validation during transition.
- Step 4 polish:
  - Add `install_alias(alias, target)` in transactor; add tests.
  - Add a tx-fn wrapper for retractEntity (calls the grammar operation);
- Step 7 next:
  - Implement AVET/VAET segment builders and integrate merge thresholds; add scans and tests.
