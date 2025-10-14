#!/usr/bin/env python3
import json
import math
import uuid
from pathlib import Path

import edb_encoding as enc


def to_hex(b: bytes) -> str:
    return b.hex()


def write_json(path: Path, obj):
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def main():
    out = Path(__file__).parent / "vectors"
    out.mkdir(parents=True, exist_ok=True)

    # doubles
    doubles = [
        ("-inf", float("-inf")),
        ("-1.5", -1.5),
        ("-0.0", -0.0),
        ("0.0", 0.0),
        ("1.5", 1.5),
        ("+inf", float("inf")),
        ("NaN", float("nan")),
    ]
    write_json(
        out / "double.json",
        {
            "value_type": "DOUBLE",
            "vectors": [
                {
                    "value": name,
                    "bytes_hex": to_hex(enc.encode_scalar(val, enc.ValueType.DOUBLE)),
                }
                for name, val in doubles
            ],
        },
    )

    # longs
    longs = [
        ("min_i64", -(1 << 63)),
        ("-1", -1),
        ("0", 0),
        ("1", 1),
        ("max_i64", (1 << 63) - 1),
    ]
    write_json(
        out / "long.json",
        {
            "value_type": "LONG",
            "vectors": [
                {
                    "value": name,
                    "bytes_hex": to_hex(enc.encode_scalar(val, enc.ValueType.LONG)),
                }
                for name, val in longs
            ],
        },
    )

    # strings NFC
    s_pre = "\u00E9"  # "é" precomposed
    s_decomp = "e\u0301"  # decomposed
    write_json(
        out / "string_nfc.json",
        {
            "value_type": "STRING",
            "vectors": [
                {
                    "value": "\u00E9",
                    "bytes_hex": to_hex(enc.encode_scalar(s_pre, enc.ValueType.STRING)),
                },
                {
                    "value": "e\\u0301",
                    "bytes_hex": to_hex(enc.encode_scalar(s_decomp, enc.ValueType.STRING)),
                },
            ],
        },
    )

    # keyword
    kw = ":ns/name"
    write_json(
        out / "keyword.json",
        {
            "value_type": "KEYWORD",
            "vectors": [
                {"value": kw, "bytes_hex": to_hex(enc.encode_scalar(kw, enc.ValueType.KEYWORD))}
            ],
        },
    )

    # uuid
    u = uuid.UUID("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    write_json(
        out / "uuid.json",
        {
            "value_type": "UUID",
            "vectors": [
                {
                    "value": str(u),
                    "bytes_hex": to_hex(enc.encode_scalar(u, enc.ValueType.UUID)),
                }
            ],
        },
    )

    # uint8
    write_json(
        out / "uint8.json",
        {
            "value_type": "UINT8",
            "vectors": [
                {"value": 0, "bytes_hex": to_hex(enc.encode_scalar(0, enc.ValueType.UINT8))},
                {"value": 255, "bytes_hex": to_hex(enc.encode_scalar(255, enc.ValueType.UINT8))},
            ],
        },
    )

    # tuple RGBA (uint8)
    rgba = [120, 40, 255, 128]
    tb = enc.encode_tuple_v1(rgba, enc.ValueType.UINT8)
    write_json(
        out / "tuple_rgba.json",
        {
            "elem_type": "UINT8",
            "value": rgba,
            "tuple_bytes_hex": to_hex(tb),
            "v_bytes_hex": to_hex(enc.tuple_order_bytes(tb)),
        },
    )

    # tuple of strings (homogeneous variable-length)
    ss = ["a", "é"]
    tb2 = enc.encode_tuple_v1(ss, enc.ValueType.STRING)
    write_json(
        out / "tuple_strings.json",
        {
            "elem_type": "STRING",
            "value": ss,
            "tuple_bytes_hex": to_hex(tb2),
            "v_bytes_hex": to_hex(enc.tuple_order_bytes(tb2)),
        },
    )

    print(f"Wrote vectors to {out}")

    # bigint
    bigs = [
        ("-255", -255),
        ("-1", -1),
        ("0", 0),
        ("1", 1),
        ("255", 255),
        ("2^200", 1 << 200),
    ]
    write_json(
        out / "bigint.json",
        {
            "value_type": "BIGINT",
            "vectors": [
                {
                    "value": name,
                    "bytes_hex": to_hex(enc.encode_scalar(val, enc.ValueType.BIGINT)),
                }
                for name, val in bigs
            ],
        },
    )

    # decimal
    dec_inputs = ["0", "1.0", "-0.0", "10.01", "-10.0100", "1e10", "-1.2345e-6"]
    write_json(
        out / "decimal.json",
        {
            "value_type": "DECIMAL",
            "vectors": [
                {
                    "value": s,
                    "bytes_hex": to_hex(enc.encode_scalar(s, enc.ValueType.DECIMAL)),
                }
                for s in dec_inputs
            ],
        },
    )

    # float32/16/bfloat16
    floats = [
        ("-inf", float("-inf")),
        ("-1.5", -1.5),
        ("-0.0", -0.0),
        ("0.0", 0.0),
        ("1.5", 1.5),
        ("+inf", float("inf")),
        ("NaN", float("nan")),
    ]
    write_json(
        out / "float32.json",
        {
            "value_type": "FLOAT32",
            "vectors": [
                {
                    "value": name,
                    "bytes_hex": to_hex(enc.encode_scalar(val, enc.ValueType.FLOAT32)),
                }
                for name, val in floats
            ],
        },
    )
    write_json(
        out / "float16.json",
        {
            "value_type": "FLOAT16",
            "vectors": [
                {
                    "value": name,
                    "bytes_hex": to_hex(enc.encode_scalar(val, enc.ValueType.FLOAT16)),
                }
                for name, val in floats
            ],
        },
    )
    write_json(
        out / "bfloat16.json",
        {
            "value_type": "BFLOAT16",
            "vectors": [
                {
                    "value": name,
                    "bytes_hex": to_hex(enc.encode_scalar(val, enc.ValueType.BFLOAT16)),
                }
                for name, val in floats
            ],
        },
    )


if __name__ == "__main__":
    main()
