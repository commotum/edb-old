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
  - Internal: raw bytes; ordered by bytes (note: equality-only indexing).
  - Limitations: cannot be unique and cannot be used in lookup refs.
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

---

Extended scalar types (growth-only, with canonical encodings)

Status: Spec-only. These extend the MVP set. Each entry defines external forms (EDN/JSON) and a canonical internal byte encoding that preserves total ordering via lexicographic byte comparison. Unless explicitly noted, all integers/floats use big-endian encodings and all strings are UTF-8 NFC.

- :db.type/bigint (arbitrary-precision integer)
  - External
    - EDN: integer literal of any size.
    - JSON: canonical base-10 string without leading zeros (except "0"). Accept JSON number only when |n| ≤ 2^53-1; otherwise require string.
  - Internal (canonical, order-preserving)
    - Normalize: remove all leading zeros from magnitude (0 → special case).
    - Encoding layout: sign marker + length + magnitude bytes.
      - sign: 0x00 = negative, 0x01 = zero, 0x02 = positive.
      - length: minimal big-endian varuint of the magnitude byte length (L≥1). For zero, omit length+magnitude.
      - magnitude: big-endian bytes of |n| without leading zeros.
    - Ordering transform
      - Positive (sign 0x02): emit [0x02][varuint(L)][magnitude].
      - Zero (sign 0x01): emit single byte [0x01].
      - Negative (sign 0x00): emit [0x00][inv(varuint(L))][inv_bytes(magnitude)], where inv(x) = bitwise-NOT of each byte. This inverts both length and magnitude so that lexicographic byte order matches numeric order across negatives (more negative → sorts earlier).
    - Notes
      - varuint is base-128, big-endian groups with continuation bit 0x80 set on all but the last byte; canonical = minimal-length (no leading 0x80 groups with value 0).
      - Examples (bytes shown hex): 0 → [01]; 255 → [02 01 FF]; -255 → [00 FE 00].

- :db.type/decimal (arbitrary-precision base-10 decimal)
  - External
    - EDN: decimal literal or tagged (e.g., #db/decimal "-123.4500").
    - JSON: canonical base-10 string with optional leading "-" and fractional part; no leading "+"; no leading zeros unless value is 0; no trailing zeros after decimal point; no decimal point for integers; optional exponent "e±k" allowed but canonicalized on ingest to plain coefficient form.
  - Internal (canonical, order-preserving)
    - Normalize to scientific-integer form: value = s × D × 10^(E - (len(D) - 1))
      - s ∈ {-1, 0, +1}
      - D is a non-empty sequence of base-10 digits with no leading zeros (except D="0" for zero). Remove trailing zeros from D for non-zero values.
      - E = floor(log10(|value|)) for non-zero values; for zero: special case.
    - Encoding layout: sign marker + exponent + digits-length + digits.
      - sign: 0x00 = negative, 0x01 = zero, 0x02 = positive.
      - exponent: big-endian varint with bias so that lex order tracks numeric order across different E.
        - Compute Eb = E + EXP_BIAS. EXP_BIAS = 2^20 (1,048,576) for ample headroom; adjust if larger E ranges are required in practice.
        - Encode Eb as big-endian varuint; for negative values, encode inv(varuint(Eb)).
      - digits-length: big-endian varuint of len(D) (≥1 for non-zero). For negative values, encode inv(varuint(len(D))).
      - digits: D encoded in packed BCD, two digits per byte (high nibble first), last nibble 0xF if odd count (canonical). For negative values, apply inv_bytes to each byte of the packed digits.
    - Ordering
      - Positive: [0x02][varuint(Eb)][varuint(len(D))][BCD(D)].
      - Zero: [0x01].
      - Negative: [0x00][inv(varuint(Eb))][inv(varuint(len(D)))][inv_bytes(BCD(D))].
      - With this transform, lexicographic byte order equals numeric order (most negative → … → zero → … → most positive).
    - Notes
      - Canonicalization removes all fractional trailing zeros and disallows "-0"; "-0.0" maps to 0.
      - BCD encoding uses nibbles 0x0..0x9 only; 0xF pad nibble appears only at the end when digit count is odd.

- :db.type/float32
  - External: EDN/JSON number (advisory; beware precision in JSON). When declared float32, downcast from wider types is explicit.
  - Internal: IEEE 754 binary32. Canonicalization: normalize all NaNs to a single quiet NaN payload 0; map -0.0 → +0.0.
  - Order-preserving byte transform T(u32): let x be the 32-bit pattern; if sign bit set, T = bitwise-NOT(x); else T = x XOR 0x80000000. Compare T as unsigned big-endian.
  - Upsert caveat: NaN cannot be compared for upsert; retract before asserting a new value when changing from/to NaN.

- :db.type/float16 and :db.type/bfloat16
  - External: tagged literal or string (e.g., #db/float16 "1.5").
  - Internal: IEEE 754 binary16 and bfloat16 respectively; canonicalize NaNs and -0.0 as for float32.
  - Ordering: same transform as float32 on the corresponding bit width (flip sign domain to monotonic order).

- :db.type/complex64 and :db.type/complex128
  - External: tagged object {"re": <float>, "im": <float>} or EDN {:re x :im y}.
  - Internal: pair of floats (float32/float64) each canonicalized as above; bytes = T(re) || T(im).
  - Ordering: lexicographic pair (re first, then im). Note: many indexes will use equality-only for complex values; ordering is defined for total order consistency.

- :db.type/date (UTC calendar date, no time)
  - External: ISO-8601 calendar date "YYYY-MM-DD".
  - Internal: days since Unix epoch (1970-01-01) as signed 32-bit or 64-bit integer; big-endian. Ordering: numeric.

- :db.type/local-time (time of day, no zone)
  - External: ISO-8601 local time "HH:MM:SS[.fraction]" (up to 9 fractional digits).
  - Internal: nanoseconds since local midnight as unsigned 64-bit; big-endian. Ordering: numeric.

- :db.type/local-date-time (no zone)
  - External: ISO-8601 "YYYY-MM-DDTHH:MM:SS[.fraction]" (no offset).
  - Internal: date-days (i64) || time-nanos (u64). Ordering: lex by (date, time).

- :db.type/duration
  - External: ISO-8601 duration (e.g., "P3DT4H5M6.123S"). Canonicalization converts months/years to exact seconds only when unambiguous; otherwise prefer second/nanosecond pair.
  - Internal: total nanoseconds as BigInt using :db.type/bigint canonical encoding (signed). Ordering: numeric via bigint ordering.

- :db.type/timezone-id
  - External: IANA TZ identifier string (e.g., "Europe/Paris").
  - Internal: canonical string form lowercased, UTF-8 NFC; ordered by bytes. Validation against IANA database is recommended at ingest time.

- :db.type/ulid
  - External: Crockford base32 ULID string (26 chars).
  - Internal: 16 bytes: 48-bit big-endian timestamp (ms) || 80-bit randomness; ordered by bytes (time-ordered).

- :db.type/hash256
  - External: hex (lowercase, even length) or base64url without padding.
  - Internal: 32 bytes; ordered by bytes.

- :db.type/inet (IP address)
  - External: IPv4/IPv6 string per RFC 5952 (compressed IPv6 permitted). For IPv4-in-IPv6, accept mapped forms; emit canonical IPv6 text on round-trip.
  - Internal: 16 bytes IPv6; map IPv4 into IPv6-mapped space (::ffff:a.b.c.d). Ordering: bytes.

- :db.type/cidr (IP prefix)
  - External: CIDR string (e.g., "192.168.0.0/16", "2001:db8::/32").
  - Internal: inet(16 bytes) || prefix-length (u8). Ordering: bytes (address first, then prefix).

- :db.type/bytes-fixed-32 (example fixed-size binary)
  - External: base64 (JSON) or tagged literal (EDN).
  - Internal: exactly 32 bytes; ordering: bytes. Typical use: content hashes.

- :db.type/json (document, equality-indexed only)
  - External: JSON value.
  - Internal: canonical JSON per RFC 8785 (JCS) serialized to UTF-8 and then optionally to a compact binary (e.g., CBOR canonical). Indexing: equality/hashing only; ordering is undefined/not supported.

- :db.type/edn (document, equality-indexed only)
  - External: EDN value.
  - Internal: canonical EDN serialization (key order canonicalized; numeric/keyword/string encodings canonical). Indexing: equality/hashing only.

Encoding reference notes
- varuint (big-endian, base-128): for each byte, high bit 1 indicates more bytes follow; last byte has high bit 0. The remaining 7 bits per byte form a big-endian integer. Minimal encoding required (no redundant leading 0 groups).
- inv(varuint(x)) means bitwise-NOT of each byte in the canonical varuint encoding of x; inv_bytes(b0..bn) means apply bitwise-NOT to each byte.
- Ordered-float transform generalizes across widths (16/32/64): if sign bit is 1, invert all bits; else flip sign bit to 1. Compare as unsigned big-endian.
- Strings are normalized to NFC; consider future collation support if locale-aware ordering is needed.

Interoperability guidance
- JSON: avoid lossy round-trips for 64-bit and larger integers; prefer strings for :db.type/bigint and for :db.type/long when outside JS safe integer. Floats remain JSON numbers.
- NaN/−0.0 normalization: ensures stable hashing/signing across platforms.
- Time values: prefer UTC instants for moments in time (:db.type/instant); use date/local-time/local-date-time/duration for calendar math without zone ambiguity.

Limitations & caveats
- :db.type/bytes: equality-only; not unique; not for lookup refs.
- NaN cannot be compared for upsert; change by retract then assert.
