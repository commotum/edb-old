# Datomic Reverse Engineer — Overview

## Table of Contents
- [North Star](#north-star)
- [Purpose](#purpose)
- [Design Docs](#design-docs)
- [Structure — Reverse Engineer Folder Summary](#structure)
- [Datomic Reference — Folder Summary](#datomic-reference-—-folder-summary)
- [Mentat — Folder Summary](#mentat-—-folder-summary)
- [PostgreSQL Storage Setup Scripts — Folder Summary](#postgresql-storage-setup-scripts-—-folder-summary)
- [Highlights](#highlights)
- [Notes & Limitations](#notes--limitations)
- [Related](#related)

This repository includes a workspace for inspecting and reverse‑engineering portions of Datomic Pro’s peer library. The `datomic-reverse-engineer` folder aggregates decompiled sources, helper tools, and artifacts to study Datomic internals across storage backends, indexing, and the query engine.

## North Star
- What if creating a database were as easy as creating a struct or class — and Datomic’s time‑traveling, replayable model ran everywhere on everyday defaults (WASM, SQLite/Postgres, HTTP/gRPC), with first‑class peer‑to‑peer sync and effortless, evolvable schemas for arbitrary relations and consumers?

## Design Docs
- Value Types (scalars): DOCS/research/value-types.md — canonical scalar types (including `:db.type/uint8`), external EDN/JSON forms, and internal ordering/encoding notes.
- Tuple Encoding: DOCS/research/tuple-encoding.md — spec for canonical tuple/composite encoding, accessors, ordering, and SQL pushdown guidance.

## Purpose
- Provide a browsable, decompiled view of Datomic Peer (v1.0.7277) internals.
- Surface implementation details for Datalog/pull, indexing, storage backends (DynamoDB/S3, SQL, Cassandra/H2), and peer/cluster components.
- Bundle tooling (Bronsa’s tools.decompiler and CFR output) used to extract and analyze bytecode.

## Structure
- `datomic-reverse-engineer/linux-install.sh` — Convenience script to install the Clojure CLI tools on Linux.
- `datomic-reverse-engineer/src-java/` — Large set of decompiled Java sources for Datomic Peer; includes:
  - `datomic/**` packages covering query (Datalog/Pull), index/write paths, integrity checks, KV/cluster adapters, AWS (DDB/S3), Cassandra, H2, Lucene, process/monitoring, I/O, crypto, and utilities.
  - `com/datomic/**` Java internals, e.g. `com/datomic/impl/peer/ActiveMQInputStream.java` bridging `ActiveMQBuffer` to `InputStream`.
  - `summary.txt` — CFR 0.152 decompile report for `peer-1.0.7277.jar`, noting many methods “Unable to fully structure code”.
- `datomic-reverse-engineer/sql-src/` — Decompiled Java for the `datomic.sql` helpers (e.g., `sql$connect.java`, `sql$select.java`, `sql$execute_commands.java`).
- `datomic-reverse-engineer/sql-classes/` — Matching compiled `.class` files for the SQL helpers.
- `datomic-reverse-engineer/tools/` — Embedded decompiler project and artifacts:
  - `tools.decompiler/` — Bronsa’s tools.decompiler (Clojure + Java) with `project.clj`, `deps.edn`, sources, and build output.
  - Prebuilt jars: `tools.decompiler-0.1.0*.jar`, `tools.decompiler-0.1.0-alpha1-standalone.jar`.
- `datomic-reverse-engineer/src-clj/` — Placeholder for Clojure sources (currently empty).

## Highlights
- Broad coverage of Datomic subsystems: Datalog evaluation, pull API, indexing (e.g., `index$write_vals.java`), storage adapters (`ddb*`, `s3*`, `kv_sql*`, `cassandra_v4*`, `h2*`), catalog/log functions, integrity tools, and monitoring.
- SQL layer is present both as decompiled source and as compiled classes for reference.
- Inclusion of decompiler sources enables repeatable extraction and deeper analysis beyond the provided outputs.

## Notes & Limitations
- Decompiled code is for exploration/learning; it is not a buildable replacement for Datomic.
- The CFR report (`src-java/summary.txt`) indicates numerous methods that could not be fully structured by the decompiler; semantics may be incomplete or misleading.
- Class/package names and method identifiers often reflect Clojure-compiled naming (e.g., `$fn__NNN`), which can be noisy.

## Related
- Datomic version targeted: Peer 1.0.7277 (per `src-java/summary.txt`).
- Decompilers: CFR (report) and Bronsa’s `tools.decompiler` (included under `tools/`).

## Datomic Reference — Folder Summary

The `datomic-reference` folder contains curated reference material, diagrams, and tutorials distilled from the official Datomic documentation. It provides conceptual overviews, best practices, and topic‑specific guides.

- Top‑level docs:
  - `datomic-reference/overview.md` — Intro to Datomic (information model, architecture, editions, APIs) with links and diagrams (`overview.dot`, `overview.png`).
  - `datomic-reference/best_practices.md` — Modeling and operational guidance (schema growth, uniqueness, enums, noHistory, aliases).
  - `datomic-reference/entities.md` — Entities and datoms, identity, and projection concepts.
  - `datomic-reference/programming_with_data_and_edn.md` — Data/EDN usage patterns.
  - `datomic-reference/time_in_datomic.md` — Time model, t/tx, as‑of/since/history.
  - Diagrams: `datomic-reference/datomic.dot`/`datomic.png`, `datomic-reference/newDB.dot`/`newDB.png`.

- Subdirectories:
  - `datomic-reference/indexes/` — Indexes overview, model, and background indexing (`1_indexes_toc.md`, `2_index_model.md`, `3_background_indexing.md`).
  - `datomic-reference/query_and_pull/` — Query execution and Pull API (`1_query_toc.md`, `2_executing_queries.md`, `3_query.md`, `4_pull.md`).
  - `datomic-reference/schema/` — Schema fundamentals, changing schema, data modeling, identity/uniqueness (`1_schema.md`–`4_identity_and_uniqueness.md`).
  - `datomic-reference/transactions/` — Transactions TOC, model, data, processing, transaction functions, ACID, synchronization, partitions, and latency hints (`1_transactions_toc.md`–`9_reducing_latency_with_transaction_hints.md`).
  - `datomic-reference/operation/` — Storage and transactor docs, plus a multi‑step tutorial (`operation/tutorial/1_tutorial_toc.md` … `8_history.md`).
  - `datomic-reference/technicals/` — Technical notes: outer joins, querying byte arrays, composing transactions by example, comparison with updating transactions, writing problem reports.

Notes:
- Most files include front‑matter metadata (title, source URL, created date) and are intended as learning/reference material rather than runnable code.

## Mentat — Folder Summary

The `mentat` folder contains Mozilla’s unmaintained Project Mentat: a Rust implementation of a Datomic/DataScript‑inspired embedded knowledge base. It offers EDN parsing, a typed core, a SQLite‑backed store, Datalog‑style querying (algebrizer → abstract SQL → projector), optional syncing, FFI bindings, a CLI, SDKs, and extensive docs.

- Overview
  - `mentat/README.md` — Project description, motivation, build/test usage (`cargo build`, `cargo test --all`).
  - Root `Cargo.toml` — Mentat crate v0.11.1. Features: `bundled_sqlite3`, `sqlcipher`, `syncable`. Workspace members: `tools/cli`, `ffi`.
  - License: Apache 2.0 (`mentat/LICENSE`).

- Core crates
  - `edn/` — EDN parser and transaction input preparation.
  - `core/` (`mentat_core`) — Core types, SQL value mappings, utilities, common DB keywords.
  - `db/` (`mentat_db`) and `sql/` (`mentat_sql`) — Store and SQL abstraction (SQLite; optional SQLCipher).
  - `transaction/` — Transacting entities/datoms; query helpers and result types.
  - Traits crates: `core-traits/`, `db-traits/`, `sql-traits/`, `public-traits/`.

- Query engine
  - `query-algebrizer/` — Translates parsed queries + schema into algebra over Mentat’s SQL schema.
  - `query-sql/` — Abstract SQL representation produced by the algebrizer.
  - `query-projector/` — Projects SQL results into Datalog outputs.
  - `query-pull/` — Pull API implementation. Each has corresponding `*-traits/` crates.

- Syncing (optional)
  - `tolstoy/` and `tolstoy-traits/` — Sync support behind the `syncable` feature.

- Tooling and interfaces
  - `tools/cli/` — `mentat_cli` REPL/CLI.
  - `ffi/` — C FFI (`mentat_ffi`) building `lib`, `staticlib`, `cdylib`.
  - `sdks/` — Platform SDKs (e.g., Android).
  - `docs/` — Static site with API docs for Rust, Swift, and Android Java.

- Notable API surface
  - `src/lib.rs` re‑exports key types and macros (`kw!`, `var!`), conditionally exposes sync types, and wires crates together.

Notes
- Marked “UNMAINTAINED” by Mozilla; suitable for reference/forking rather than a maintained dependency.

## PostgreSQL Storage Setup Scripts — Folder Summary

The `datomic-postgresql-storage-setup-scripts` folder contains minimal SQL scripts to bootstrap a PostgreSQL database for Datomic’s SQL storage adapter.

- Files
  - `postgres-user.sql` — Creates a login role `nemo` with password `nemo` (example credentials; change for any real use).
  - `postgres-db.sql` — Creates database `mycloud` owned by `postgres`, UTF‑8, `template0` base, default tablespace.
  - `postgres-table.sql` — Creates key/value table `mycloud_kvs`:
    - Columns: `id text PRIMARY KEY`, `rev integer`, `map text`, `val bytea`.
    - Sets owner `postgres`; grants ALL to `postgres` and `public`.

- Intended use
  - Illustrates the minimal K/V schema Datomic’s SQL store expects (`id` key, optional `rev` and `map`, and binary `val`).
  - Use as a starting point; adjust database name, role ownership, privileges, and credentials for your environment.
  - Typical setup order: create role → create database → create table → tighten grants.

- Notes
  - Scripts are examples; do not use the default `nemo/nemo` credentials or wide `GRANT ALL` in production.
