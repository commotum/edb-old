Golden Vectors

This folder will hold canonical hex encodings and JSON descriptors for cross‑runtime validation.

Suggested files
- double.json: inputs including -inf, -1.5, -0.0, 0.0, 1.5, +inf, NaN → hex
- long.json: min, -1, 0, 1, max → hex
- string_nfc.json: "é" precomposed/decomposed → identical hex
- keyword.json: ":ns/name" → hex
- uuid.json: RFC example → hex
- uint8.json: 0, 255 → hex
- tuple_rgba.json: [120,40,255,128] elemType=UINT8 → full tuple bytes hex and V* hex

