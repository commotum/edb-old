# Goal 10 — Kernel Identity and Valid-State Repair

## Objective

Repair the load-bearing semantic kernel so transaction time and entity identity
cannot alias, callers cannot mint unissued permanent IDs, schema changes cannot
acknowledge an invalid successor database, and accepted transitions retain
their meaning through canonical encoding, indexes, and exact PostgreSQL
recovery.

## Constraints

- Follow `goal-9/EVIDENCE_LEDGER.md` entries C01, C02, C04–C07.
- Use `datomic_pro_docs` as semantic authority and recovered
  `datomic.db/make-eid`, `t->tx`, `get-ids`, `filter-assess-tx-datoms`,
  `Db.addData`, tuple validators, and typed comparators as the default blueprint.
- Keep `Database::with` pure and transaction input unordered. Ordinary
  attribute/type resolution stays on db-before; final invariants use the
  complete proposed database and successor schema.
- Rust and PostgreSQL only. Existing database/wire compatibility is not
  required, but durable format changes must be explicitly versioned and fail
  closed rather than silently reinterpreted.
- Do not implement general schema-as-data here; Goal 11 owns that migration.
  Leave a clean identity/schema boundary for it.
- Preserve the useful current kernel and unrelated dirty-tree work. Add the
  smallest structural machinery needed for correctness.

## Known context

- Current transaction entities equal raw basis values and collide with the
  ordinary allocator at transaction 1000.
- Current explicit IDs are accepted without an issuance check, while allocation
  starts at 1000 and advances only from tempids.
- Current transaction processing calculates `successor_schema` but validates
  final facts with `self.schema`; recovery validates the successor and can reject
  an acknowledged transaction.
- Current tuple installation checks only shape and a few general attribute
  properties. Recovered tuple validators are stricter.
- Transaction/map normalization currently uses `Debug` strings. Encoding
  already contains structural canonical encoders that can be reused or moved.
- Persistent index ordering needs stored-value tie handling for BigDecimal scale.

## Stages

### 1. Executable failure witnesses

**Status:** Complete.

**Outcome:** Focused tests reproduce the identity collision, unissued-ID
acceptance, successor-schema invalid commit, tuple gaps, and stored-value tie.

**Focus:** Small pure fixtures first; durable round-trip fixture where the bug
crosses recovery; assertions based on docs/source rather than current behavior.

**Completion signal:** Each confirmed defect has a failing regression test with
the intended semantic outcome explicit.

### 2. Encoded identity and issued frontier

**Status:** Complete.

**Outcome:** Transaction, system, and user entities occupy disjoint identity
domains and all conversions/allocation are checked and propagated consistently.

**Focus:** Exact recovered 42-bit eid structure where sound; `t->tx`/`tx->t`;
frontier semantics; tempid allocation; explicit ID validation; temporal views,
history chunks, encoding, query transaction values, excision cutoffs, and index
manifests.

**Completion signal:** Transaction 1000 cannot alias any user entity, future
explicit IDs fail, issued-but-currently-empty IDs remain usable, overflow fails
structurally, and all identity-aware tests pass.

### 3. Successor-schema and tuple validity

**Status:** Complete.

**Outcome:** Every successful transition is valid under its resulting schema,
while db-before resolution timing remains intact.

**Focus:** Final cardinality/uniqueness/composite validation with successor
schema; tuple legal types and constituents; immutable tuple definitions;
discontinuation timing; recovery equivalence.

**Completion signal:** Conflicting schema/data transactions abort before
return/publication, legal changes succeed, and no accepted report can fail
`Database::validate_invariants` or durable replay.

### 4. Structural determinism and stored distinctions

**Status:** Complete.

**Outcome:** Semantic processing no longer depends on Rust `Debug`, and index
construction/loading preserves storage-distinct values with equal semantic sort
position.

**Focus:** Typed canonical operation/form/map ordering or truly set-based
processing; deterministic error selection without printed representations;
BigDecimal scale tie-breakers across in-memory and persistent indexes.

**Completion signal:** Debug-independent permutation fixtures pass, canonical
bytes remain stable, and cardinality-many scale variants survive base
construction, consolidation, and recovery.

### 5. Durable and integrated closure

**Status:** Complete.

**Outcome:** Every changed representation and invariant is exercised across the
PostgreSQL boundary without silently skipped evidence.

**Focus:** Encoding version; corruption checks; create/commit/recover/restart;
property/permutation checks; compatibility failure behavior; plan/evidence
fold-back.

**Completion signal:** Formatting, warning-denying clippy, pure tests, and an
explicitly executed PostgreSQL goal-10 suite pass; Goal 9 Stage 1 can be marked
complete with no known identity or accepted-invalid-state gap.

## Exit condition

Goal 10 completes only when collision-free identity and successor-schema
validity hold in the pure kernel, canonical durable representation, indexes,
and real PostgreSQL recovery, with source-backed regression evidence and no
passing-by-skip integration claim.

## Completion evidence — 2026-09-03

- `tests/identity_repair.rs` covers partitioned `t`/tx/user identity,
  transaction 1000, checked issuance, issued-but-empty reuse, time views,
  query transaction bindings, and canonical durable encoding version 2.
- `tests/successor_schema_repair.rs` first reproduced and now guards the two
  acknowledged-invalid-state paths: many-to-one and uniqueness alterations
  combined with conflicting data in one transaction.
- `tests/tuple_schema_repair.rs` has nine fixtures for the recovered tuple
  scalar set, successor-schema constituent checks, definition immutability,
  and discontinuation timing.
- Transaction/form normalization uses typed structural comparison rather than
  `Debug`. Datom/index ordering and invariant comparison use strict stored
  ties, and `tests/stored_value_repair.rs` retains `1.0M` and `1.00M` through
  the log, index consolidation, a one-entry peer cache, and recovery.
- `goal-10/IDENTITY.md` records the one material deviation from recovered
  `nextT`: Atomic keeps contiguous PostgreSQL logical `t` values and a separate
  exclusive entity-index frontier. This is permitted by the documented
  monotonic-time contract and avoids coupling SQL log sequence to tempid count.
- `cargo fmt --all -- --check`, `cargo check --all-targets --offline`, full
  `cargo test --offline`, and warning-denying clippy passed. The full serialized
  suite was then run with `ATOMIC_POSTGRES_URL` against an isolated PostgreSQL
  15.11 instance; all tests passed (the crash-worker helper is intentionally
  ignored and invoked by its parent test). Both explicit `pg_ctl restart`
  fixtures also ran and passed, rather than taking their missing-environment
  early return.

**Exit status:** Complete. Goal 9 Stage 1 may advance to Goal 11.
