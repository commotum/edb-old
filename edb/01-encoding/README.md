EDB Encoding (v1)

Scope
- Canonical, total, order-preserving encodings for core scalars and tuple v1.
- encode(value) -> bytes and decode(bytes) -> value for supported types.
- Total order by lexicographic comparison of encoded bytes (after transform for floats).

Supported types (MVP)
- :db.type/long (signed 64-bit)
- :db.type/double (IEEE 754; NaN normalization; -0.0 -> +0.0; order transform)
- :db.type/boolean (0x00/0x01)
- :db.type/string (UTF-8 NFC; length-prefixed)
- :db.type/keyword (canonical string form as above)
- :db.type/uuid (16 bytes)
- :db.type/instant (epoch micros; signed 64-bit)
- :db.type/ref (entid; signed 64-bit)
- :db.type/bytes (opaque; equality-only indexing)
- :db.type/uint8 (single byte 0..255)
- tuple v1 (homogeneous slots): header excluded from ordering; V* compared lexicographically

Non-goals (now)
- bigint/decimal, float32/16/bfloat16, date/local-time/local-date-time/duration, ulid/inet/json/edn.

Order rules
- Integers: big-endian two's-complement canonical for fixed widths; zigzag not needed for fixed width.
- Floats: transform bit-pattern so that lexicographic byte order matches numeric: if sign bit set -> invert all bits; else flip sign bit to 1. Normalize NaN to single quiet NaN; map -0.0 to +0.0.
- Strings/keywords: UTF-8 NFC; prefix with minimal varuint length; then bytes; compare by bytes.
- tuple v1: [TT=tuple][ver=0x01][arity][S*][V*]; compare only V* bytes.

Tests
- Round-trip encode/decode across all types
- Ordering: sort by value, then ensure encoded bytes are in lexicographic order
- Golden vectors in tests/vectors covering: doubles (NaN/-0), strings (NFC), keywords, uuid, uint8, tuples (RGBA)

CLI (future)
- Optional small tool to encode/decode and print hex for vectors

