# EDB Core Map (Stage 2)

This document is a consolidated, technically detailed map of the EDB core crates reviewed so far. It focuses on architecture, data flow, core invariants, and the responsibilities of each module.

## Scope and Sources

- CORE/edb-encoding
- CORE/edb-edn
- CORE/edb-envelope
- CORE/edb-index
- CORE/edb-pull
- CORE/edb-query
- CORE/edb-schema
- CORE/edb-server
- CORE/edb-store-sqlite
- CORE/edb-transactor
- CORE/edb-tx

## System Overview

EDB is a single-node, SQLite-backed database with:

- A deterministic binary encoding for ordered value bytes (`edb-encoding`).
- An EDN parser for values, queries, and tx entity forms (`edb-edn`).
- A minimal EDN query executor for conjunctive patterns over indexes (`edb-query`).
- A transaction model and validation pipeline (`edb-tx`).
- A transactor that writes current state, unique indexes, and a durable log, and updates secondary indexes (`edb-transactor`).
- A small SQLite segment store for index segments and metadata (`edb-store-sqlite`).
- Multiple read indexes (EAVT, AEVT, AVET, VAET) for scanning and range queries (`edb-index`).
- A pull API for structured entity projections, including reverse refs and nested traversal (`edb-pull`).
- A CBOR-based envelope format for signed transactions (`edb-envelope`).
- An HTTP server exposing transact, pull, query, and streaming APIs (`edb-server`).

## Core Data Model

### Entities, Attributes, and Values

- Entities are identified by `i64` entids.
- Attributes are defined in schema with ident, value type, cardinality, uniqueness, and component semantics.
- Values are strongly typed via `edb_tx::model::Value` and mapped to `edb_encoding::ValueType`.

### Schema Layer (`edb-schema`)

- Core types: `Attribute`, `Catalog`, `AttrCardinality`, `AttrUnique`.
- SQLite adapter (feature `sqlite`): `SchemaCatalog` loads `attrs` and `aliases` tables into a `Catalog` plus alias map.
- Mentat-style access trait: `HasSchema` exposes `attribute_for_ident`, `canonical_ident`, `identifies_attribute`, and `component_attributes`.
- Type inference helper: `ValueTypeSet` supports unions/intersections and numeric type sets.
- `HasSchema::attribute_value_type_set` returns a single-type set for known attributes.
- Alias resolution: `HasSchema::attribute_for_ident` resolves alias idents to canonical idents.
- Used as the canonical schema source for query/algebrizer integration, with component attr list precomputed.

### Transaction Operations

Supported operations (at the model level):

- `Add { e, a, v }`
- `Retract { e, a, v? }`
- `Cas { e, a, expected?, v }`

Transaction primitives are the normalized, concrete datoms applied to storage:

- `TxPrimitive { added, e, a, v }`

### Uniqueness and Cardinality

- Uniqueness is enforced during validation (`AttrUnique::Identity` or `AttrUnique::Value`).
- Cardinality-one attributes imply implicit retract of previous value when a new value is asserted.
- Uniqueness uses a computed `value_key` in the `unique_idx` table.

## Encoding Layer (`edb-encoding`)

### Value Types

`ValueType` enumerates the system value types:

- Long, Double, Boolean, String, Keyword, Uuid, Instant, Ref, Bytes, Uint8, Bigint, Decimal, Float32, Float16, Bfloat16
- Centralized `attrs.vt` decoding via `ValueType::try_from(i64)` to avoid duplicate mapping tables.

### Ordering-Preserving Binary Encodings

Primary goal: produce bytes that sort correctly lexicographically to preserve value ordering.

Key rules:

- `Long` (i64): bias by `2^63` and encode as big-endian u64.
- `Double` (f64): canonicalize NaN, map -0.0 to +0.0, then sign-bit flip/invert for ordering.
- `Float32/Float16/Bfloat16`: similar sign-bit treatment with canonical NaN and zero normalization.
- `String`: NFC-normalize, length-prefix with big-endian varuint, then UTF-8 bytes.
- `Keyword`: same as String but enforced `:` prefix.
- `Uuid`: 16 bytes.
- `Bytes`: raw bytes (no length, used carefully in tuple decoding).
- `Uint8`: 1 byte.
- `Bigint`: sign byte, varuint length, magnitude bytes; negative values invert bytes for order.
- `Decimal`: sign byte, biased exponent, length, BCD digits; negative values invert bytes.

### Tuple Encoding (v1)

- Header: `0xF1 0x01` + arity + repeated type codes.
- Payload: concatenated element encodings.
- `tuple_order_bytes` returns payload region for ordering.

### CLI Tool

`edb-enc` supports:

- `encode <TYPE> <VALUE>`
- `encode-tuple <TYPE> <CSV_VALUES>`
- `decode <TYPE> <HEX>`

### Tests

Vector tests validate deterministic encodings, including NFC string normalization and tuple ordering bytes.

## EDN Layer (`edb-edn`)

### Parsing and Types

- PEG-based EDN parser (peg 0.8) for values, queries, and tx entity forms.
- Scalars: nil, booleans, integers, bigints (`N`), decimals (`M`), floats, strings, keywords, symbols, instants (`#inst`), UUIDs (`#uuid`).
- Collections: lists, vectors, maps, sets; commas as whitespace; line comments.

### Query Parsing

- List-form queries `[:find ... :where ...]` parsed into `edb_edn::query::ParsedQuery`.
- Map-form queries `{:find [...] :where [...]}` normalized to list-form semantics for parsing.
  - `:find` vectors expand to relation find-elems; collection `...` vectors are preserved.
- Supports `:in` binding forms (scalar/coll/tuple/rel), rules var `%`, pattern-name inputs, and rule expressions in `:where`.
- Supports return maps (`:keys`, `:strs`, `:syms`), `:order` (asc/desc), and `:limit` (including `nil`).
- Supports `or`/`or-join`, `not`/`not-join`, predicates, function bindings, type annotations, and pull in `:find`.

### Transaction Entity Parsing

- Parses EDN tx entities `[:db/add ...]`, `[:db/retract ...]`, and map notation `{:db/id ... :attr ...}` into `edb_edn::entities`.

## Envelope Layer (`edb-envelope`)

### Envelope Format

`UnsignedEnvelopeV1` fields:

- `magic`: "edb.tx"
- `version`: 1
- `parents`: vec of 32-byte hashes
- `features`: declared feature flags (must be empty for now)
- `author_pubkey`: 32 bytes
- `authored_at`: optional timestamp
- `tx_body`: canonical, CBOR-sorted ops

### Canonicalization

- Each `EnvOp` is encoded as a CBOR array `[op, args...]`.
- Ops are sorted by their CBOR byte representation before inclusion.
- JSON objects are converted to CBOR with keys sorted for determinism.

### Signing and Verification

- `to_unsigned_bytes` emits a fixed-order CBOR array.
- `tx_id` is SHA-256 of unsigned bytes.
- `sign` and `verify` use Ed25519.

### Conversion Helper

`env_ops_from_txops` converts `edb_tx::model::TxOp` into `EnvOp`, serializing entity refs as JSON (entid, tempid, lookup tuple).

## Storage Layer (`edb-store-sqlite`)

### Tables

- `segments(id, val)` for immutable segment blobs.
- `roots(name, rev, val)` for CAS-protected root pointers.
- `log(seq, val)` for append-only transaction log.

### Traits

- `SegmentStore`: `get_segment`, `put_segment_if_absent`.
- `RootStore`: `get_root`, `init_root`, `cas_root`.
- `LogStore`: `append_log`, `read_log_range`.

### Behavior

- WAL + FULL synchronous for durability.
- Roots support optimistic CAS using `rev`.
- Log seq is autoincremented and used for ordered reads.

## Index Layer (`edb-index`)

### Common Pattern

Each indexer:

- Buffers datoms in memory.
- Sorts deterministically.
- Writes sorted segments as JSON into `edb-store-sqlite`.
- Maintains a root listing segment IDs.
- Compacts when segments exceed a threshold.

Datoms store value bytes as base64 (no padding).

### Attribute Sort Key

Attributes are ordered using NFC-normalized bytes from `edb-encoding::encode_scalar_string`, with the varuint length prefix stripped.

### Indexes and Ordering

- EAVT: (E, A, V, T desc)
  - `scan_entity(e)`
  - `scan_ea(e, a)`
  - `scan_eav_prefix(e, a, v_prefix)`

- AVET: (A, V, E, T desc)
  - `scan_av_eq(a, v_bytes)`
  - `scan_av_range(a, v_start, v_end)`

- VAET (refs only): (V, A, E, T desc)
  - `scan_v(v_e)`

- AEVT: (A, E, V, T desc)
  - `scan_a(a)`
  - `scan_ae(a, e)`

### Value Encoding

- Values are encoded using `edb-encoding` with ordered bytes, then stored as base64 strings.

## Transaction Layer (`edb-tx`)

### Model Types

- `Value`: strongly typed scalar values.
- `EntityRef`: entid, tempid, or lookup ref.
- `TxOp`: add/retract/cas.
- `TxPrimitive`: concrete datom-level operations.
- `TxReport`: tx metadata, tempids, touched attrs, primitives, meta.

### Grammar Normalization

`normalize_grammar` supports:

- Array ops: `add`, `retract`, `cas`, `tx-fn`, `tx-meta`, `retract-entity`.
- Map-form entities: `{ "db/id": ..., ":ns/attr": ... }`.
- Tempids and lookup refs.
- Type-coerced values based on schema.

`retract-entity` cascades over component refs using `DbView.entity_attrs`.

### EDN Normalization

`normalize_edn` supports:

- EDN tx forms `[:db/add ...]`, `[:db/retract ...]`, and map notation `{:db/id ... :attr ...}`.
- Vector values expand into multiple ops for cardinality-many attributes.
- Ref values resolve tempids and lookup refs during normalization.
- EDN tx functions, CAS, and nested map notation are not yet implemented.
- Bytes policy: EDN string literals are treated as raw bytes for `ValueType::Bytes`.

### Validation

`normalize_and_validate` enforces:

- Attribute existence.
- Type matching vs `ValueType`.
- Uniqueness (`Identity`, `Value`).
- CAS expected-vs-current.
- Cardinality-one implicit retracts.

### Allocation

- `EntidAllocator` and `TempResolver` manage new entids and tempid bindings.

## Transactor Layer (`edb-transactor`)

### Storage Tables

- `attrs`: schema definitions.
- `current`: current value per (e, a).
- `unique_idx`: uniqueness index keyed by `value_key`.
- `meta`: counters and txInstant tracking.
- `aliases`: alias -> canonical ident.
- Envelope tables: `tx_envelopes`, `tx_edges`, `heads`.

### Apply Transaction Flow

1. Normalize grammar and validate ops.
2. Allocate `tx_eid` and compute monotonic `txInstant`.
3. Apply primitives to `current` and `unique_idx`.
4. Append log entry `{ primitives, meta, tx_eid, tx_instant }`.
5. Update `head` root.
6. Apply primitives to indexes (EAVT/AEVT/AVET/VAET).
7. Record merge/compaction latency stats.
8. Broadcast `TxReport` to subscribers.

### Envelope Submission (Linear Mode)

- Decode CBOR envelope; verify `magic`/`version` and empty `features`.
- Verify Ed25519 signature.
- Compute `tx_id` and enforce `parents` == current head (or genesis).
- Store envelope and edges; update `heads`.
- Decode CBOR ops into `TxOp`, validate, and apply primitives.
- Mark envelope as applied.

### Database Views

- `db()`: current view.
- `as_of(t)`: reconstruct state using log up to `t`.
- `since(t)`: state changes after `t`.
- `history_db()`: all primitives for entity.

### Replay

`replay_current_from_log` rebuilds `current` and `unique_idx` from the log.

## Pull Layer (`edb-pull`)

### Attribute Specs

`AttrSpec` supports:

- Direct `Attr` and `Reverse`.
- `Nested` and `ReverseNested` with sub-specs.
- `AttrAs` (alias) and `AttrDefault`.
- `NestedLimit` and `ReverseNestedLimit` with per-spec `limit` and `max_depth`.

### Puller Behavior

- Reads schema from `attrs`/`aliases`.
- Uses `current` for scalar values.
- Uses `edb-index::EavtIndexer` when DB path is provided for multi-valued and ref scans.
- Expands component refs by default.
- Produces JSON output with reverse refs prefixed by `_`.

## Server Layer (`edb-server`)

### Runtime Model

- Single background worker serializes all DB operations.
- SSE stream (`/subscribe`) broadcasts tx reports.
- Metrics track tx rates and index merge/compaction behavior.

### HTTP Endpoints

- `POST /transact`: JSON tx ops.
- `POST /transact-edn`: EDN tx entities.
- `POST /submit-envelope`: signed envelope submission.
- `GET /db`: current basis `t`.
- `POST /sync`: wait for basis >= target.
- `GET /heads`: current tx heads.
- `GET /tx/:txid`: stored envelope.
- `POST /pull`: structured pull queries.
- `POST /q`: minimal query planner.
- `GET /metrics`, `GET /health`.

### Query Planner (`/q`)

- Uses AVET for value constraints (eq, between, ge).
- Uses AEVT for existence constraints (`has`).
- Supports a single ref-var join for `Ref` attributes.
- Produces either entity sets or joined pairs.

### Minimal EDN Query Prototype (`edb-query`)

- `edb-query` provides a prototype EDN path for conjunctive data patterns (multiple `:where` clauses).
- Uses AVET for value-constant scans, EAVT for entity-constant scans, and AEVT for attribute scans.
- Supports entity/value variables and `_` placeholders with equality joins across shared variables.
- Supports basic predicates (`=`, `!=`, `<`, `<=`, `>`, `>=`) plus `or` and `not` over data patterns.
- Uses `edb-edn` parsing and `edb-schema::sqlite` for attribute/type lookup.
- `:find` supports variables only (rel/coll/tuple/scalar), with fixed `:limit`; no `:in`, `:with`, `:order`, rules, or where-fn bindings yet.

## Cross-Cutting Invariants and Ordering Rules

- Attribute ordering uses NFC-normalized bytes (string encoding sans length prefix).
- Value ordering relies on `edb-encoding` ordered byte encodings.
- Log ordering is authoritative for `as_of`, `since`, and `history` views.
- Index segments are immutable; roots point to segment lists with CAS updates.
- Envelope mode is currently linear (single head).

## Test Coverage Highlights

- Encoding vectors for all scalar and tuple formats.
- Index scan correctness and range behavior.
- Pull behavior for nested specs and component expansion.
- Transactor flows: tempids, lookups, replay, aliases, txInstant monotonicity.
- Envelope validation and linear-head enforcement.
- Query joins and existence checks.

## Notes and Open Points

- Float types are normalized to `Value::Double` for validation, and encoding preserves ordering.
- `Bytes` values are equality-only in schema; uniqueness is disallowed at install time.
- Envelope `features` are currently rejected if non-empty.
- Query planner does not implement gt/lt/le yet and supports only one ref-var join.
