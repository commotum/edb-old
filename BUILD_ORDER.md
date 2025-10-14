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

