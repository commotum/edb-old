import math
import uuid

import edb_encoding as enc


def lex(b1: bytes, b2: bytes) -> int:
    return (b1 > b2) - (b1 < b2)


def test_long_roundtrip_and_order():
    vals = [-(1 << 63), -123, -1, 0, 1, 42, (1 << 63) - 1]
    encs = [enc.encode_scalar(v, enc.ValueType.LONG) for v in vals]
    decs = [enc.decode_scalar(b, enc.ValueType.LONG) for b in encs]
    assert decs == vals
    # Ensure lex ordering matches numeric
    for i in range(len(vals) - 1):
        assert lex(encs[i], encs[i + 1]) < 0


def test_double_roundtrip_and_order():
    vals = [
        float("-inf"),
        -1.5,
        -0.0,
        0.0,
        1.5,
        float("inf"),
    ]
    encs = [enc.encode_scalar(v, enc.ValueType.DOUBLE) for v in vals]
    # -0.0 maps to +0.0 on decode
    decs = [enc.decode_scalar(b, enc.ValueType.DOUBLE) for b in encs]
    assert math.isinf(decs[0]) and decs[0] < 0
    assert decs[1] == -1.5
    assert decs[2] == 0.0
    assert decs[3] == 0.0
    assert decs[4] == 1.5
    assert math.isinf(decs[5]) and decs[5] > 0
    # Order
    for i in range(len(vals) - 1):
        assert lex(encs[i], encs[i + 1]) < 0
    # NaN normalization encodes to canonical
    n1 = enc.encode_scalar(float("nan"), enc.ValueType.DOUBLE)
    n2 = enc.encode_scalar(float("nan"), enc.ValueType.DOUBLE)
    assert n1 == n2


def test_boolean_roundtrip_and_order():
    f = enc.encode_scalar(False, enc.ValueType.BOOLEAN)
    t = enc.encode_scalar(True, enc.ValueType.BOOLEAN)
    assert enc.decode_scalar(f, enc.ValueType.BOOLEAN) is False
    assert enc.decode_scalar(t, enc.ValueType.BOOLEAN) is True
    assert lex(f, t) < 0


def test_string_nfc_and_roundtrip_order():
    # "é" precomposed vs decomposed should normalize to NFC
    s1 = "\u00E9"  # precomposed
    s2 = "e\u0301"  # decomposed
    b1 = enc.encode_scalar(s1, enc.ValueType.STRING)
    b2 = enc.encode_scalar(s2, enc.ValueType.STRING)
    assert b1 == b2
    d = enc.decode_scalar(b1, enc.ValueType.STRING)
    assert d == "é"
    # Order by bytes: "a" < "b"
    a = enc.encode_scalar("a", enc.ValueType.STRING)
    b = enc.encode_scalar("b", enc.ValueType.STRING)
    assert lex(a, b) < 0


def test_keyword_roundtrip():
    k = ":ns/name"
    kb = enc.encode_scalar(k, enc.ValueType.KEYWORD)
    kd = enc.decode_scalar(kb, enc.ValueType.KEYWORD)
    assert kd == k


def test_uuid_roundtrip_and_order():
    u = uuid.UUID("f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    b = enc.encode_scalar(u, enc.ValueType.UUID)
    assert enc.decode_scalar(b, enc.ValueType.UUID) == u
    # Order: lower bytes compare lower
    u2 = uuid.UUID(int=u.int + 1)
    b2 = enc.encode_scalar(u2, enc.ValueType.UUID)
    assert lex(b, b2) < 0


def test_uint8_roundtrip_and_order():
    b0 = enc.encode_scalar(0, enc.ValueType.UINT8)
    b1 = enc.encode_scalar(255, enc.ValueType.UINT8)
    assert enc.decode_scalar(b0, enc.ValueType.UINT8) == 0
    assert enc.decode_scalar(b1, enc.ValueType.UINT8) == 255
    assert lex(b0, b1) < 0


def test_tuple_v1_rgba_uint8():
    rgba = [120, 40, 255, 128]
    tb = enc.encode_tuple_v1(rgba, enc.ValueType.UINT8)
    vt, vals = enc.decode_tuple_v1(tb)
    assert vt == enc.ValueType.UINT8
    assert vals == rgba
    # Order compares only V*
    vbytes = enc.tuple_order_bytes(tb)
    assert vbytes.endswith(bytes(rgba))

