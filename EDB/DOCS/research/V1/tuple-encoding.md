# Tuple / Composite Value Encoding (v1)

Status: Spec-only (MVP). No code.

## Goals
- Single-blob `V` encoding where lexicographic order over bytes == tuple slot order.
- Deterministic, cross-runtime canonicalization for hashing/signing (P2P).
- Compact, typed, and sortable; optional SQL pushdown via generated columns.

MVP constraint
- Homogeneous tuples only — all slots share the same scalar type (e.g., RGBA as four `uint8`). Future phases may lift this to allow heterogeneous per-slot types.

## Wire vs Internal
- External (EDN/JSON)
  - EDN: vector `[v0 v1 ...]`.
  - JSON: array `[v0, v1, ...]`.
  - Pull: vector by default; labeled map if `:db/tupleLabels` exist.
- Internal bytes (v1)
  - `[0xTT(tuple)][0x01(version)][0xAA(arity)][S* (AA slot descriptors)][V* (concatenated slot values)]`
  - Each slot descriptor `S = <type-tag><nullable-flag>`.
  - Ordering compares `V*` lexicographically; header excluded from compare.
  - MVP note: slot descriptors must all carry the same type tag (homogeneous). Implementations MAY still encode repeated identical descriptors for stability.

## Scalar Types
- `:db.type/uint8` — unsigned 8-bit integer (0..255)
  - Canonical: single byte 0x00..0xFF; numeric sort.
  - External EDN/JSON: integers 0..255.
- Reuse existing canonical encodings for ints, doubles, bools, strings (UTF-8 NFC, length-prefixed), refs, uuid, keywords, etc.
- Floats: IEEE 754 canonical; normalize NaN; `-0.0 -> +0.0`.

## Constraints
- No hard arity cap in the spec. Implementations optimize common small composites first.
- Changing `:db/tupleTypes` or arity is a breaking schema change; migrate via new attribute + alias.
- `:db/fulltext` not applicable to tuples.
- `:db/unique` applies to the whole tuple value.
- MVP: homogeneous tuples only. Schema sugar `:db/tupleElemType` + `:db/tupleArity` normalizes to a repeated `:db/tupleTypes` vector. Do not specify both `:db/tupleTypes` and `:db/tupleElemType` in one attribute.

Limitations and recommendations
- :db.type/bytes is excluded from tuple v1: the scalar encoding for BYTES is raw (no length prefix), so tuple payloads cannot be decoded into slot boundaries. If you need binary slots in tuples, prefer fixed‑size binary types such as `:db.type/bytes-fixed-32` (and 16/64), which are tuple‑friendly and preserve ordering by bytes.
- For best performance and SQL pushdown, prefer fixed‑width slot types (e.g., `uint8`, `long`, `double`, `float32`, `float16`, `bfloat16`, `uuid`) so tuple byte offsets are O(1). Variable‑length slot types (e.g., `string`, `keyword`, `bigint`, `decimal`) remain supported, but payload size varies.

## Datalog Accessors (Spec Only)
- `(tuple/slot ?t i ?x)` binds slot `i` (0..arity-1) to `?x`.
- `(tuple/get ?t :label ?x)` resolves label to index using `:db/tupleLabels` at algebrize time; lowers to `tuple/slot`.
- Aggregations treat tuples as atomic unless accessors are used.

## SQL Pushdown (Advisory)
- Default: bytes in `v BLOB`; equality/range via byte ops.
- Optional: generated columns for hot tuple attrs.
  - Postgres: `GENERATED ALWAYS AS (tuple_decode_slot(v, i)) STORED` + B-tree indexes.
  - SQLite: generated/virtual columns + UDF; index as needed.
- Planner rewrites `(> (tuple/slot ?v 0) 200)` to `v_slot0 > 200` when column exists; otherwise fallback.
 - MVP: pushdown considerations simplify for homogeneous numeric tuples (e.g., uint8 RGBA).

## Examples
- RGBA: `:db/tupleTypes [:db.type/uint8 :db.type/uint8 :db.type/uint8 :db.type/uint8]`, labels `[:r :g :b :a]`; internal `V*` = 4 bytes `[r][g][b][a]`.
- Geo point: `[:db.type/double :db.type/double]`, labels `[:lon :lat]`.
- Range: `[:db.type/long :db.type/long]`, labels `[:start :end]`; invariant `start <= end` validated via deterministic tx-fn.

## Interop & P2P
- Feature flags: `"tuple"`, `"tuple-enc:v1"`, `"uint8"`.
- Hashing/signing cover encoded `V` bytes as part of the canonical tx envelope.
- Older peers without declared support must fail-closed on decode or request downgrade.
 - Lifting the homogeneous-only constraint will be gated behind a feature flag (TBD) and versioned layout.

## Cross-references (See also)
- Requirements: EDB-REQUIREMENTS.md
- Value types (scalars): value-types.md
- Indexing strategy: indexing-strategy.md
- P2P sync MVP: p2p-sync-mvp.md
 - Schema sugar: see EDB-REQUIREMENTS.md (tupleElemType/tupleArity)

## Open Questions
- Index hints per slot to drive generated columns automatically?
- Byte layout versioning strategy; cross-runtime conformance test matrix.

<!-- TODO(tuple): Add byte layout diagram and generated column examples. -->
