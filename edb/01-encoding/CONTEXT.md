Context

References
- docs/research/value-types.md: canonical external forms and internal encodings
- docs/research/tuple-encoding.md: tuple v1 layout and ordering
- docs/research/indexing-strategy.md: lexicographic ordering requirements

Constraints
- Encodings must be deterministic and stable
- Lexicographic byte order must equal semantic order for each type
- Floats: normalize NaN payloads and -0.0
- Strings/keywords: normalize to NFC before encoding
- tuple v1: homogeneous slots only; header excluded from compare

Interoperability
- Golden vectors capture canonical forms (hex)
- Test vectors include tricky values: NaN, -0.0, surrogate-like code points (disallowed), combining marks (NFC), max/min 64-bit longs, uint8 edges 0/255

