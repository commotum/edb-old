
---

EDB/CORE/edb-encoding

- edb-encoding is a Rust crate that defines a sortable, deterministic binary encoding for scalar values and a v1 tuple format; it exposes encode_scalar, decode_scalar, encode_tuple_v1, decode_tuple_v1, and
tuple_order_bytes via src/lib.rs.
- Scalar encoding (src/scalar.rs) is built around order-preserving byte transforms: i64 is biased into unsigned big-endian; floats use sign-bit flipping/inversion with canonical NaN and -0→+0; strings are NFC-
normalized, length-prefixed with a big-endian varuint, and JSON-ish \uXXXX escapes are unescaped for vector files.
- Supported scalar types include long, double, boolean, string, keyword (:-prefixed), uuid (16 bytes), instant/ref (long), bytes (raw), uint8, bigint, decimal, float32/float16/bfloat16. Bigint and decimal use
sign markers and varuint lengths; negatives invert encoded bytes for ordering; decimals store biased exponent and BCD digits length.
- Tuple v1 (src/tuple.rs) encodes a header 0xF1 0x01, arity, type code(s), then concatenated element encodings; decoding is type-homogeneous with payload length validation; tuple_order_bytes returns the
payload region for ordering.
- CLI tool (src/bin/edb-enc.rs) provides encode, decode, and encode-tuple for hex I/O, with simple parsing for symbolic floats and string/uuid/decimal/bigint values.
- Tests (tests/vectors.rs) load JSON vector fixtures (current or legacy path) to validate scalar and tuple encodings, including NFC string normalization and tuple ordering bytes.

---

EDB/CORE/edb-envelope

- edb-envelope defines a CBOR-based transaction envelope format with deterministic ordering and signing/verification helpers.
- EnvOp is a canonicalized, serde-tagged enum of transaction operations (add, retract, cas, retract-entity) that serialize as CBOR arrays for stable wire representation.
- UnsignedEnvelopeV1 contains metadata (magic string, version, parents, features, author pubkey, authored_at) and a tx_body of ops; constructor sorts ops by their CBOR byte representation to ensure canonical
ordering.
- CBOR encoding is explicitly constructed to enforce field order and deterministic map ordering; JSON values are converted to CBOR with key sorting for stability.
- to_unsigned_bytes emits a fixed-order CBOR array representing the envelope; tx_id hashes the unsigned bytes with SHA-256; sign/verify use Ed25519.
- Helper env_ops_from_txops converts edb_tx::model::TxOp into EnvOp, with entity refs serialized into JSON (entid, tempid, or lookup tuple).

---

EDB/CORE/edb-index

- CORE/edb-index/src/lib.rs implements four indexers over datoms using a shared pattern: in‑memory buffering, deterministic sorting, JSON‑encoded segment storage in edb_store_sqlite, root pointers per index
name, and compaction when segment count exceeds a threshold.
- Index types and sort order:
    - EAVT (EavtIndexer): sort by entity, attribute (NFC-normalized bytes), value bytes, then transaction time descending; supports scans by entity, by entity+attribute, and by entity+attribute+value prefix.
    - AVET (AvetIndexer): sort by attribute, value bytes, entity, time desc; supports equality and range scans on value bytes.
    - VAET (VaetIndexer): only ref attributes; sort by referenced entity, attribute, entity, time desc; supports scan by referenced entity.
    - AEVT (AevtIndexer): sort by attribute, entity, value bytes, time desc; supports scan by attribute and by attribute+entity.
- Values are encoded using edb-encoding into order-preserving byte sequences, then stored as base64 (no padding) strings; attribute sort keys are the NFC-encoded string bytes with the varuint length prefix
stripped.
- All indexers optionally look up attribute value types from an attrs SQLite table to ensure consistent encoding (and to detect ref types for VAET).
- Tests validate scan behavior, range queries across numeric types (double, instant, bigint, decimal), and that AEVT compaction preserves results.

---

EDB/CORE/edb-pull

- edb-pull implements a Datomic-style pull API over a SQLite-backed store, returning JSON projections of entities based on an attribute spec tree with forward, reverse, nested, aliasing, defaults, and per-spec
limits.
- AttrSpec supports direct attributes, reverse refs (_attr), nested forward/reverse pulls, limit/max-depth overrides per spec, aliases, and default values.
- Puller queries attribute metadata from attrs/aliases, reads current values from current, and uses edb-index (EAVT) when a DB path is provided to support multi-valued and ref scans with deduping/added
semantics.
- Component refs are expanded by default: if an attribute is Ref and is_component, the pull returns a nested object for the referenced entity (with recursive depth control).
- Values are normalized to plain JSON via to_plain_json, decoding edb_tx::model::Value or raw JSON; bytes are base64 encoded.
- Tests cover forward/reverse pulls, nested refs, alias/default behavior, per-spec limits, and component expansion.

---

EDB/CORE/edb-schema

- edb-schema defines the core schema types used across the system.
- AttrCardinality and AttrUnique model cardinality (one/many) and uniqueness constraints (none/identity/value).
- Attribute captures schema metadata for a datom attribute: ident, value type (edb-encoding::ValueType), cardinality, uniqueness, component flag, no-history, doc, and alias idents; it also exposes is_unique.
- Catalog is a minimal in-memory registry keyed by ident with upsert_attribute and get.
- src/lib.rs re-exports the schema types for external crates.

---

EDB/CORE/edb-server

- edb-server is a single-node HTTP server (Axum/Tokio) wrapping SqliteTransactor with a background worker that serializes writes and broadcasts tx-reports over SSE.
- Endpoints include /transact, /submit-envelope, /db, /sync, /heads, /tx/:txid, /subscribe, /metrics, /pull, /q, and /health; README.md documents request/response formats and examples.
- The main loop uses a command channel to the worker for all operations; responses flow via oneshot channels, ensuring consistent DB access and centralized metrics updates.
- Metrics track tx counts/latency, segment counts per index, and merge/compaction event counts with average merge/compaction latencies.
- /pull delegates to edb_pull::Puller with the DB path to resolve refs via indexes; /q implements a minimal query planner over AVET/AEVT with eq/between/ge/has and a single ref-var join.
- Envelope submission verifies Ed25519 signatures and enforces linear heads through SqliteTransactor::submit_envelope, and /tx/:txid exposes stored envelope blobs.

---

EDB/CORE/edb-store-sqlite

- edb-store-sqlite is a minimal SQLite-backed storage layer providing segment, root, and log primitives used by higher-level components.
- Defines traits: SegmentStore (get/put-by-id), RootStore (get/init/CAS with revision), and LogStore (append/read ranges).
- SqliteStore initializes schema (segments, roots, log) with WAL and FULL synchronous pragmas for durability.
- put_segment_if_absent uses INSERT OR IGNORE for idempotent segment writes; roots support optimistic CAS via rev.
- Log entries are append-only with autoincrementing seq and range reads ordered by sequence.

---

EDB/CORE/edb-transactor

- edb-transactor is the core SQLite transactor: it normalizes/validates tx ops (via edb_tx), writes current and unique_idx, appends a log entry, updates a head root, and incrementally feeds EAVT/AEVT/AVET/VAET
indexers with merge/compaction latency accounting.
- Schema/metadata live in local tables (attrs, current, unique_idx, meta, aliases) with monotonic txInstant tracking; alias resolution is enforced for reads and writes, with validation to prevent collisions.
- Provides DB view helpers (db, as_of, since, history_db) and log-based reconstruction (replay_current_from_log, as_of_entity_attrs, since_entity_attrs, history_entity) built on the durable log.
- Envelope submission supports signed CBOR envelopes: verifies signature, enforces linear head, stores envelope + edges, decodes CBOR ops into TxOps, applies them, and marks envelopes applied.
- Uniqueness keys are derived from value type (e.g., S:, L:); bytes use length only in value_key.
- Tests cover smoke flows (tempids, lookups, subscribe, replay), alias behavior, monotonic t and txInstant meta, as‑of/since/history views, log replay, and envelope validation (bad sig, unknown feature, parent mismatch).

---

EDB/CORE/edb-tx

- edb-tx defines the transaction model (Value, EntityRef, TxOp, TxPrimitive, TxReport), the DbView trait for accessing current state, and a normalization/validation pipeline.
- grammar.rs parses JSON tx forms into TxOps: supports array ops (add, retract, cas, tx-fn, tx-meta, retract-entity cascade) and map-form entity assertions; resolves refs with tempids/lookup refs and coerces
values based on attribute ValueType.
- validate.rs performs type checking, uniqueness enforcement, CAS checks, and expands implicit retracts for cardinality-one; also binds tempids via unique identity pre-pass and resolves entity refs.
- allocator.rs provides entid allocation and tempid resolution; txfn.rs allows custom tx functions via TxFnRegistry.
- ValueType mapping is consistent with edb-encoding (including Bigint/Decimal), with float types treated as Value::Double at validation time.
- Tests cover grammar parsing, map form, tx-fn expansion, cardinality-one retracts, type mismatch errors, lookup ref resolution, unique identity upserts, unique value conflicts, and CAS semantics.
