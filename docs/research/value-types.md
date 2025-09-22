# EDB Value Types (Scalars)

Status: Spec-only (MVP). No code.

Purpose
- Define the canonical scalar value types, external representations (EDN/JSON), and high-level internal encoding/ordering rules used by EDB.
- Favor growth-only evolution: add new types when they make domain sense; do not overload ill-fitting types.

Guiding principles
- Typed, canonical, sortable encodings enable consistent indexing (EAVT/AVET/AEVT/VAET) and time-travel semantics.
- External wire forms remain simple (EDN/JSON); internal encodings are canonical for comparison, hashing/signing, and storage.

Initial scalar set (MVP)
- :db.type/long
  - 64-bit signed integer.
  - External: EDN/JSON integer.
  - Internal: big-endian canonical; numeric ordering.
- :db.type/double
  - 64-bit IEEE 754 floating-point.
  - External: EDN/JSON number.
  - Internal: IEEE 754 canonical; normalize NaN to single quiet NaN; map -0.0 → +0.0; ordered by canonical byte transform.
- :db.type/boolean
  - External: EDN true/false; JSON true/false.
  - Internal: 0x00 / 0x01.
- :db.type/string
  - External: EDN/JSON string.
  - Internal: UTF-8 NFC, length-prefixed; ordered by bytes.
- :db.type/keyword
  - Namespaced or plain keywords.
  - External: EDN keyword; JSON string of canonical name.
  - Internal: canonical string form (UTF-8 NFC) or interned token; ordered by bytes/token ordering.
- :db.type/uuid
  - External: EDN/JSON UUID string.
  - Internal: 16 bytes; ordered by bytes.
- :db.type/instant
  - UTC instant in time.
  - External: EDN inst/ISO-8601; JSON ISO-8601 string (advisory) or number if API chooses epoch.
  - Internal: epoch micros (advisory) or canonical time representation; ordered numerically.
- :db.type/ref
  - Entity reference.
  - External: EDN lookup ref or entid; JSON number or structured lookup depending on API.
  - Internal: entid (numeric); ordered numerically.
- :db.type/bytes
  - Opaque binary.
  - External: base64 (JSON) or tagged literal (EDN) as appropriate.
  - Internal: raw bytes; ordered by bytes (note: usually not indexed except equality).
- :db.type/uint8 (new)
  - Unsigned 8-bit integer; valid range 0..255.
  - External: EDN/JSON integer 0..255.
  - Internal: single byte; numeric ordering.

Notes
- Tuples/composites use these scalars per slot; see tuple-encoding.md for canonical tuple encoding and ordering.
- Full-text indexing applies to :db.type/string only; not applicable to tuples.
- Future types (growth-only, not MVP): decimal/BigInt, geospatial, bytes fixed-size, varint, etc.

P2P & interoperability
- New types are advertised via feature flags (e.g., "uint8"); envelopes encode canonical bytes and are signed/hashed.

Cross-references (See also)
- Requirements: EDB-REQUIREMENTS.md
- Tuple encoding: tuple-encoding.md
- Indexing strategy: indexing-strategy.md
- P2P sync MVP: p2p-sync-mvp.md

Open questions
- Canonical external form for instants across APIs (ISO-8601 vs epoch micros)?
- Additional scalar types (decimal, big-int) and their canonical ordering.
