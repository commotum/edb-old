# EDB Research Log & Checklist

North Star
- What if creating a database were as easy as creating a struct or class — and Datomic’s time‑traveling, replayable model ran everywhere on everyday defaults (WASM, SQLite/Postgres, HTTP/gRPC), with first‑class peer‑to‑peer sync and effortless, evolvable schemas for arbitrary relations and consumers?

How to review each item
- Purpose: what is this for?
- Key insights: invariants, algorithms, APIs relevant to EDB
- Dependencies: libraries, storage, runtime
- Constraints: performance, consistency, single‑writer, CAS, indexing behavior
- Risks/gaps: limitations or unclear parts
- Actions: concrete next steps (prototype/spec/change/open question)
- Produce: 1‑paragraph summary + bullet insights + actions

Meta
- Related docs: insights.md, p2p-sync-mvp.md, indexing-strategy.md

## Repo Survey & Tooling
- [x] Root purpose and tooling (`README.md`, `main.py`, `pyproject.toml`)
  - Summary
    - Root overview names the project “EDB” (README.md:1). Python entrypoint prints a greeting (main.py:1) and minimal Python packaging is present (pyproject.toml:1) requiring Python 3.10 (.python-version:1).
    - Repo content centers on Datomic research and prototypes: reverse engineering (datomic-reverse-engineer), reference docs (datomic-reference), Mentat (Rust), and Postgres K/V setup scripts.
    - Tooling mix: Python (basic), Rust (Mentat workspace), SQL, Bash, Graphviz DOT diagrams, Java/Clojure artifacts for decompiled Datomic code, and a Clojure CLI installer script.
  - Key insights
    - This repo is a research workspace rather than a single build target; multiple languages and ecosystems coexist.
    - Python is currently non-essential; Rust and documentation are the primary assets; Java/Clojure pieces are artifacts for study, not to be built.
    - Graphviz can render `.dot` architecture diagrams; scripts for rendering could improve DX.
  - Actions
    - Define a minimal dev environment doc (Rust stable, Python 3.10, Graphviz, Java JRE for browsing decompiled code, optional Clojure CLI if needed).
    - Add simple Makefile/scripts to render DOT to PNG/SVG and to open docs (optional).
    - Keep Python lightweight unless an analysis/CLI emerges; avoid unnecessary Python deps.

## PostgreSQL Storage Setup Scripts
Files: `datomic-postgresql-storage-setup-scripts/`
- [ ] `postgres-user.sql` — role creation
  - [ ] Summary + insights (security posture, creds policy)
  - [ ] Actions (harden grants/roles)
- [ ] `postgres-db.sql` — DB creation
  - [ ] Summary + insights (encoding, template, ownership)
  - [ ] Actions (env‑specific names/owners)
- [ ] `postgres-table.sql` — K/V table
  - [ ] Summary + insights (id/rev/map/val, CAS expectations)
  - [ ] Actions (narrow grants, indexes, migrations)

## Datomic Reference (Ground Truth)
Folder: `datomic-reference/`
- Overviews
  - [ ] `overview.md`
  - [ ] `entities.md`
  - [ ] `programming_with_data_and_edn.md`
  - [ ] `time_in_datomic.md`
  - [ ] `best_practices.md`
- Indexes
  - [ ] `indexes/1_indexes_toc.md`
  - [ ] `indexes/2_index_model.md`
  - [ ] `indexes/3_background_indexing.md`
- Query & Pull
  - [ ] `query_and_pull/1_query_toc.md`
  - [ ] `query_and_pull/2_executing_queries.md`
  - [ ] `query_and_pull/3_query.md`
  - [ ] `query_and_pull/4_pull.md`
- Schema
  - [ ] `schema/1_schema.md`
  - [ ] `schema/2_changing_schema.md`
  - [ ] `schema/3_data_modeling.md`
  - [ ] `schema/4_identity_and_uniqueness.md`
- Transactions
  - [ ] `transactions/1_transactions_toc.md`
  - [ ] `transactions/2_transaction_model.md`
  - [ ] `transactions/3_transaction_data.md`
  - [ ] `transactions/4_processing_transactions.md`
  - [ ] `transactions/5_transaction_functions.md`
  - [ ] `transactions/6_acid.md`
  - [ ] `transactions/7_synchronization.md`
  - [ ] `transactions/8_partitions.md`
  - [ ] `transactions/9_reducing_latency_with_transaction_hints.md`
- Operation
  - [ ] `operation/1_storage.md`
  - [ ] `operation/2_transactor.md`
  - [ ] `operation/tutorial/1_tutorial_toc.md` … `8_history.md`
- Diagrams
  - [ ] `datomic.dot` (classic)
  - [ ] `overview.dot` (full view)
  - [ ] `newDB.dot` (new runtimes)

For each: produce summary, key insights, constraints, actions, and open questions; feed invariants into insights.md.

## Mentat (Rust)
Folder: `mentat/`
- Top level
  - [ ] `README.md` (motivation, comparisons, build/tests)
  - [ ] `Cargo.toml` (features/workspace)
  - [ ] `src/lib.rs`, `conn.rs`, `query_builder.rs`, `store.rs`, `vocabulary.rs`
- Core crates
  - [ ] `edn/` (parser, transaction input)
  - [ ] `core/` (types, SQL mappings, utils)
  - [ ] `db/`, `sql/` (storage, abstraction)
  - [ ] `transaction/` (transact, results)
  - [ ] Traits: `core-traits/`, `db-traits/`, `sql-traits/`, `public-traits/`
- Query engine
  - [ ] `query-algebrizer/`, `query-sql/`, `query-projector/`, `query-pull/`
- Tooling/interfaces
  - [ ] `tools/cli/` (mentat_cli)
  - [ ] `ffi/` (C FFI)
  - [ ] `sdks/` (Android)
  - [ ] `docs/` (API docs)

For each: note patterns portable to EDB (even if not Rust), risks from unmaintained status, and DX ideas (CLI/FFI/SDKs).

## Datomic Reverse Engineer (Peer)
Folder: `datomic-reverse-engineer/`
- Orientation
  - [ ] `src-java/summary.txt` (CFR report)
- Query/Data path
  - [ ] `datomic/datalog$eval_query.java`, joins, projections
  - [ ] `datomic/pull$*.java`
- Indexes/Log/IO
  - [ ] `datomic/index$*.java`, `index/TransposedData.java`
  - [ ] `datomic/log$*.java`
- Storage adapters
  - [ ] `datomic/kv_sql*`, `kv_sql_ext*`, `sql-src/datomic/sql$*.java`
  - [ ] `datomic/ddb*`, `s3*`, `cassandra*`, `h2*`
- Peer/cluster/process
  - [ ] `datomic/peer$*.java`, `cluster*`, `process_monitor*`, `extensions*`
- Utilities
  - [ ] `fressian*`, `crypto*`, `lucene/*`, `datafy*`, `treewalk*`
- Java internals
  - [ ] `com/datomic/impl/peer/ActiveMQInputStream.java`
- Tools
  - [ ] `tools/tools.decompiler/*`

For each: extract invariants (tx ordering, CAS rules, index semantics), and portability deltas (JVM → WASM/HTTP, serialization alternatives). 

## P2P & Sync
- [ ] Define log model (append‑only, signed, t/causality, replay)
- [ ] Device identity & trust (keys, signatures, rotation)
- [ ] Replication flows (peer↔peer, peer↔service, conflicts)
- [ ] Consistency targets (eventual vs. partitions, per‑attr unique)
- [ ] Security/encryption (at rest/in transit)
- [ ] Offline‑first behavior (queues, backpressure)
- [ ] Observers/events (tx‑reports, subscriptions)

Output: p2p-sync-mvp.md with an MVP plan and open questions.

## Deliverables (Each Section)
- [ ] 1‑paragraph summary
- [ ] Key insights (bullets)
- [ ] Decisions/requirements for EDB
- [ ] Risks/open questions
- [ ] Proposed next actions
- [ ] File references
