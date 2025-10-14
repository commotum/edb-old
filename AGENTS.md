# Agent Playbook — EDB Workspace

Purpose
- Guide agents and contributors to work in a predictable way that matches the recommended build order and keeps reference material separate from new code.

Write Boundaries
- Writable: `edb/`, `docs/spec/`, `docs/adr/`, `tests/vectors/`, `tools/`.
- Read‑only: `datomic-reference/`, `datomic-reverse-engineer/`, `mentat/`, `docs/research/` (use for context; copy conclusions into specs/ADRs).

Always Load (every session)
- `overview.md:1`
- `docs/research/EDB-REQUIREMENTS.md:1`
- `docs/research/value-types.md:1`
- `docs/research/tuple-encoding.md:1`
- The current step’s `edb/<step>/CONTEXT.md`

Build Order (high level)
1) Core Value Encoding + Datom Types
2) Transaction Model + Validation
3) Append‑Only Log + t (SQLite)
4) Schema Catalog + Identity/Lookup + Components
5) Unique Enforcement (identity/value)
6) Memory Index + Background Indexer (EAVT)
7) AVET + VAET Indexes
8) Query Engine (parse → algebrize → plan)
9) Pull Engine
10) API Server (HTTP/gRPC)
11) Observability (metrics/logs/tx‑reports)
12) P2P Sync MVP
13) CLI/SDKs

Per‑Step Workflow
1. Open: Always‑load files above, then the step’s `README.md` and `CONTEXT.md`.
2. Tests first: author or update tests in `edb/<step>/tests/`.
3. Implement minimal code in `edb/<step>/src/` or shared in `edb/00-core/`.
4. Record decisions as short ADRs in `docs/adr/` and link from the step README.
5. Update `STATUS.md` (Done/In‑progress/Blocked/Open).

Per‑Step Acceptance Template (use in each README)
- Scope: What this step must deliver.
- Interfaces: Public functions/types/APIs.
- Dependencies: Steps this relies on.
- Tests: What to validate (correctness and ordering/perf where applicable).
- Docs to load: Canonical paths to reference material (see CONTEXT.md).

Per‑Step Context Map (additive to Always Load)
- 01‑encoding: `docs/research/value-types.md:1`, `docs/research/tuple-encoding.md:1`
- 02‑tx-model: `datomic-reference/transactions/2_transaction_model.md:1`, `datomic-reference/transactions/3_transaction_data.md:1`
- 03‑log: EDB requirements (log section) and your step README/spec
- 04‑schema: `datomic-reference/schema/1_schema.md:1`
- 05‑unique: `datomic-reference/schema/4_identity_and_uniqueness.md:1`
- 06–07 indexes: `docs/research/indexing-strategy.md:1`
- 08 query: `datomic-reference/overview.md:1`
- 09 pull: `datomic-reference/time_in_datomic.md:1`

Conventions
- Keep patches scoped to the active step. Shared types live in `edb/00-core/`.
- Do not edit reference folders; instead, distill findings into `docs/spec/` and ADRs.
- Keep specs small and precise; prefer links to references.

Testing Strategy
- Start with unit/property tests local to the step, then add cross‑step checks.
- Maintain golden vectors in `tests/vectors/` (encoding, order, tuple examples).

