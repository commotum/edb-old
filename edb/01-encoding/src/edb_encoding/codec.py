from __future__ import annotations

import math
import struct
import unicodedata
from dataclasses import dataclass
from enum import IntEnum
from typing import Iterable, List, Tuple


class ValueType(IntEnum):
    LONG = 1
    DOUBLE = 2
    BOOLEAN = 3
    STRING = 4
    KEYWORD = 5
    UUID = 6
    INSTANT = 7
    REF = 8
    BYTES = 9
    UINT8 = 10


def _nfc(s: str) -> str:
    return unicodedata.normalize("NFC", s)


def _varuint_be(n: int) -> bytes:
    if n < 0:
        raise ValueError("varuint must be non-negative")
    # base-128 big-endian groups with continuation bit 1 on all but last
    if n == 0:
        return b"\x00"
    groups: List[int] = []
    while n > 0:
        groups.append(n & 0x7F)
        n >>= 7
    groups.reverse()
    out = bytearray()
    for i, g in enumerate(groups):
        last = (i == len(groups) - 1)
        if last:
            out.append(g)
        else:
            out.append(0x80 | g)
    return bytes(out)


def _parse_varuint_be(b: bytes, offset: int = 0) -> Tuple[int, int]:
    """Return (value, next_offset)."""
    val = 0
    i = offset
    if i >= len(b):
        raise ValueError("truncated varuint")
    while True:
        byte = b[i]
        i += 1
        cont = (byte & 0x80) != 0
        val = (val << 7) | (byte & 0x7F)
        if not cont:
            break
        if i >= len(b):
            raise ValueError("truncated varuint")
    return val, i


def _i64_to_ordered_bytes(x: int) -> bytes:
    # Map signed 64-bit to unsigned order-preserving by XOR with sign bit.
    if x < -(1 << 63) or x > (1 << 63) - 1:
        raise OverflowError("i64 out of range")
    u = (x + (1 << 63)) & 0xFFFFFFFFFFFFFFFF
    return u.to_bytes(8, byteorder="big", signed=False)


def _ordered_bytes_to_i64(b: bytes) -> int:
    if len(b) != 8:
        raise ValueError("expected 8 bytes for i64")
    u = int.from_bytes(b, byteorder="big", signed=False)
    x = u - (1 << 63)
    # normalize into Python int
    if x >= (1 << 63):
        x -= (1 << 64)
    return x


def _canonical_double_bits(x: float) -> int:
    if math.isnan(x):
        # canonical quiet NaN payload 0
        return 0x7FF8000000000000
    # map -0.0 to +0.0
    if x == 0.0:
        return 0x0000000000000000
    return struct.unpack(
        ">Q", struct.pack(">d", x)
    )[0]  # big-endian IEEE 754 bits


def _double_to_ordered_bytes(x: float) -> bytes:
    bits = _canonical_double_bits(x)
    sign = (bits >> 63) & 1
    if sign == 1:
        # negative: invert all bits
        out = (~bits) & 0xFFFFFFFFFFFFFFFF
    else:
        # non-negative: flip sign bit to 1
        out = bits ^ 0x8000000000000000
    return out.to_bytes(8, byteorder="big", signed=False)


def _ordered_bytes_to_double(b: bytes) -> float:
    if len(b) != 8:
        raise ValueError("expected 8 bytes for double")
    u = int.from_bytes(b, byteorder="big", signed=False)
    sign = (u >> 63) & 1
    if sign == 1:
        # original was non-negative
        bits = u ^ 0x8000000000000000
    else:
        # original was negative
        bits = (~u) & 0xFFFFFFFFFFFFFFFF
    if bits == 0x7FF8000000000000:
        return float("nan")
    if bits == 0:
        return 0.0
    return struct.unpack(
        ">d", struct.pack(">Q", bits)
    )[0]


def encode_scalar(value, value_type: ValueType) -> bytes:
    vt = ValueType(value_type)
    if vt == ValueType.LONG:
        if not isinstance(value, int):
            raise TypeError("LONG expects int")
        return _i64_to_ordered_bytes(value)
    elif vt == ValueType.INSTANT:
        if not isinstance(value, int):
            raise TypeError("INSTANT expects epoch micros as int")
        return _i64_to_ordered_bytes(value)
    elif vt == ValueType.REF:
        if not isinstance(value, int):
            raise TypeError("REF expects entid as int")
        return _i64_to_ordered_bytes(value)
    elif vt == ValueType.DOUBLE:
        if not isinstance(value, float):
            # allow ints to coerce to float
            if isinstance(value, int):
                value = float(value)
            else:
                raise TypeError("DOUBLE expects float")
        return _double_to_ordered_bytes(value)
    elif vt == ValueType.BOOLEAN:
        if not isinstance(value, bool):
            raise TypeError("BOOLEAN expects bool")
        return b"\x01" if value else b"\x00"
    elif vt == ValueType.STRING:
        if not isinstance(value, str):
            raise TypeError("STRING expects str")
        s = _nfc(value)
        data = s.encode("utf-8")
        return _varuint_be(len(data)) + data
    elif vt == ValueType.KEYWORD:
        if not isinstance(value, str) or not value.startswith(":"):
            raise TypeError("KEYWORD expects canonical ':ns/name' string form")
        s = _nfc(value)
        data = s.encode("utf-8")
        return _varuint_be(len(data)) + data
    elif vt == ValueType.UUID:
        import uuid as _uuid

        if isinstance(value, _uuid.UUID):
            u = value
        elif isinstance(value, str):
            u = _uuid.UUID(value)
        else:
            raise TypeError("UUID expects uuid.UUID or str")
        return u.bytes
    elif vt == ValueType.BYTES:
        if not isinstance(value, (bytes, bytearray, memoryview)):
            raise TypeError("BYTES expects bytes-like")
        return bytes(value)
    elif vt == ValueType.UINT8:
        if not isinstance(value, int) or not (0 <= value <= 255):
            raise TypeError("UINT8 expects int in 0..255")
        return bytes([value])
    else:
        raise NotImplementedError(f"Unsupported type {vt}")


def decode_scalar(b: bytes, value_type: ValueType):
    vt = ValueType(value_type)
    if vt in (ValueType.LONG, ValueType.INSTANT, ValueType.REF):
        return _ordered_bytes_to_i64(b)
    elif vt == ValueType.DOUBLE:
        return _ordered_bytes_to_double(b)
    elif vt == ValueType.BOOLEAN:
        if b == b"\x00":
            return False
        if b == b"\x01":
            return True
        raise ValueError("invalid boolean encoding")
    elif vt in (ValueType.STRING, ValueType.KEYWORD):
        length, i = _parse_varuint_be(b, 0)
        data = b[i : i + length]
        if len(data) != length:
            raise ValueError("truncated string")
        s = data.decode("utf-8")
        return _nfc(s)  # ensure NFC on decode as well
    elif vt == ValueType.UUID:
        import uuid as _uuid

        if len(b) != 16:
            raise ValueError("invalid uuid bytes length")
        return _uuid.UUID(bytes=b)
    elif vt == ValueType.BYTES:
        return bytes(b)
    elif vt == ValueType.UINT8:
        if len(b) != 1:
            raise ValueError("UINT8 requires 1 byte")
        return b[0]
    else:
        raise NotImplementedError(f"Unsupported type {vt}")


# Tuple v1 encoding (homogeneous)
_TT_TUPLE = 0xF1  # arbitrary tuple tag for internal use
_TUPLE_VER = 0x01


def encode_tuple_v1(values: Iterable, elem_type: ValueType) -> bytes:
    vals = list(values)
    arity = len(vals)
    if arity == 0:
        raise ValueError("tuple arity must be >= 1")
    vt = ValueType(elem_type)
    # Slot descriptor: one byte type code (no nullable flag in MVP)
    type_code = vt.value & 0x7F
    header = bytearray()
    header.append(_TT_TUPLE)
    header.append(_TUPLE_VER)
    if arity > 255:
        raise ValueError("tuple arity too large for v1 header")
    header.append(arity)
    for _ in range(arity):
        header.append(type_code)
    # Concatenate V* for order bytes
    v_bytes = bytearray()
    for v in vals:
        v_bytes.extend(encode_scalar(v, vt))
    return bytes(header) + bytes(v_bytes)


def tuple_order_bytes(tuple_bytes: bytes) -> bytes:
    if len(tuple_bytes) < 3:
        raise ValueError("invalid tuple header")
    if tuple_bytes[0] != _TT_TUPLE or tuple_bytes[1] != _TUPLE_VER:
        raise ValueError("unsupported tuple encoding")
    arity = tuple_bytes[2]
    # header = TT(1) + VER(1) + ARITY(1) + arity * 1 (slot descriptors)
    offset = 3 + arity
    if offset > len(tuple_bytes):
        raise ValueError("truncated tuple header")
    return tuple_bytes[offset:]


def decode_tuple_v1(tuple_bytes: bytes) -> Tuple[ValueType, List]:
    if len(tuple_bytes) < 3:
        raise ValueError("invalid tuple header")
    if tuple_bytes[0] != _TT_TUPLE or tuple_bytes[1] != _TUPLE_VER:
        raise ValueError("unsupported tuple encoding")
    arity = tuple_bytes[2]
    if len(tuple_bytes) < 3 + arity:
        raise ValueError("truncated tuple header")
    # Read slot descriptor (homogeneous; use first)
    type_code = tuple_bytes[3]
    vt = ValueType(type_code)
    # decode V*
    values: List = []
    offset = 3 + arity
    vbuf = memoryview(tuple_bytes)[offset:]
    # We cannot decode without boundaries for variable-length types.
    # For MVP, support only fixed-width homogeneous tuples here (e.g., UINT8, LONG via 8 bytes, DOUBLE via 8 bytes, UUID 16 bytes).
    # For variable-length (STRING/KEYWORD/BYTES), the tuple should be used with schema-guided decode or a length prefix per slot in a future version.
    if vt in (ValueType.LONG, ValueType.INSTANT, ValueType.REF, ValueType.DOUBLE):
        width = 8
    elif vt == ValueType.UINT8:
        width = 1
    elif vt == ValueType.UUID:
        width = 16
    else:
        raise NotImplementedError(
            f"decode_tuple_v1 MVP supports fixed-width types only, got {vt.name}"
        )
    if len(vbuf) != arity * width:
        raise ValueError("tuple payload length mismatch")
    for i in range(arity):
        chunk = vbuf[i * width : (i + 1) * width].tobytes()
        values.append(decode_scalar(chunk, vt))
    return vt, values

