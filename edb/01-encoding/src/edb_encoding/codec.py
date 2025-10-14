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
    BIGINT = 11
    DECIMAL = 12
    FLOAT32 = 13
    FLOAT16 = 14
    BFLOAT16 = 15


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
    elif vt == ValueType.BIGINT:
        if not isinstance(value, int):
            raise TypeError("BIGINT expects int")
        return _encode_bigint(value)
    elif vt == ValueType.DECIMAL:
        from decimal import Decimal

        if isinstance(value, (int, float)):
            d = Decimal(str(value))
        elif isinstance(value, str):
            d = Decimal(value)
        elif isinstance(value, Decimal):
            d = value
        else:
            raise TypeError("DECIMAL expects Decimal|str|int|float")
        return _encode_decimal(d)
    elif vt == ValueType.FLOAT32:
        if not isinstance(value, float):
            if isinstance(value, int):
                value = float(value)
            else:
                raise TypeError("FLOAT32 expects float")
        return _float32_to_ordered_bytes(value)
    elif vt == ValueType.FLOAT16:
        if not isinstance(value, float):
            if isinstance(value, int):
                value = float(value)
            else:
                raise TypeError("FLOAT16 expects float")
        return _float16_to_ordered_bytes(value)
    elif vt == ValueType.BFLOAT16:
        if not isinstance(value, float):
            if isinstance(value, int):
                value = float(value)
            else:
                raise TypeError("BFLOAT16 expects float")
        return _bfloat16_to_ordered_bytes(value)
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
    elif vt == ValueType.BIGINT:
        return _decode_bigint(b)
    elif vt == ValueType.DECIMAL:
        return _decode_decimal(b)
    elif vt == ValueType.FLOAT32:
        return _ordered_bytes_to_float32(b)
    elif vt == ValueType.FLOAT16:
        return _ordered_bytes_to_float16(b)
    elif vt == ValueType.BFLOAT16:
        return _ordered_bytes_to_bfloat16(b)
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
    if vt in (
        ValueType.LONG,
        ValueType.INSTANT,
        ValueType.REF,
        ValueType.DOUBLE,
        ValueType.FLOAT32,
    ):
        width = 8
    elif vt == ValueType.UINT8:
        width = 1
    elif vt == ValueType.UUID:
        width = 16
    elif vt in (ValueType.FLOAT16, ValueType.BFLOAT16):
        width = 2
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

# ---------------- BigInt ----------------

def _encode_bigint(n: int) -> bytes:
    if n == 0:
        return b"\x01"
    neg = n < 0
    m = -n if neg else n
    mag = []
    while m > 0:
        mag.append(m & 0xFF)
        m >>= 8
    mag_bytes = bytes(reversed(mag))
    L = len(mag_bytes)
    if neg:
        # [0x00][inv(varuint(L))][inv(mag)]
        vu = _varuint_be(L)
        vu_inv = bytes((~b) & 0xFF for b in vu)
        mag_inv = bytes((~b) & 0xFF for b in mag_bytes)
        return b"\x00" + vu_inv + mag_inv
    else:
        return b"\x02" + _varuint_be(L) + mag_bytes


def _decode_bigint(b: bytes) -> int:
    if not b:
        raise ValueError("empty bigint encoding")
    sign = b[0]
    if sign == 0x01:
        return 0
    i = 1
    if sign == 0x02:
        L, i = _parse_varuint_be(b, i)
        mag = b[i : i + L]
        if len(mag) != L:
            raise ValueError("truncated bigint magnitude")
        val = 0
        for byte in mag:
            val = (val << 8) | byte
        return val
    elif sign == 0x00:
        # invert varuint bytes until a non-continued byte appears
        # To parse, we invert on the fly
        # Reconstruct inverted stream then parse
        inv_stream = bytearray()
        while True:
            if i >= len(b):
                raise ValueError("truncated negative bigint varuint")
            invb = (~b[i]) & 0xFF
            inv_stream.append(invb)
            i += 1
            if (invb & 0x80) == 0:
                break
        L, _ = _parse_varuint_be(bytes(inv_stream), 0)
        mag_inv = b[i : i + L]
        if len(mag_inv) != L:
            raise ValueError("truncated negative bigint magnitude")
        mag = bytes((~x) & 0xFF for x in mag_inv)
        val = 0
        for byte in mag:
            val = (val << 8) | byte
        return -val
    else:
        raise ValueError("invalid bigint sign byte")


# ---------------- Decimal ----------------

EXP_BIAS = 1 << 20


def _encode_decimal(d) -> bytes:
    from decimal import Decimal

    if not isinstance(d, Decimal):
        raise TypeError("_encode_decimal expects Decimal")
    if d.is_nan():
        raise ValueError("Decimal NaN unsupported")
    if d == 0:
        return b"\x01"
    # Normalize: remove trailing zeros
    d = d.normalize()
    sign = 1 if d < 0 else 2  # 1: zero, 2: positive; we use 0 for negative below
    if d < 0:
        d = -d
        sign_byte = b"\x00"
    else:
        sign_byte = b"\x02"
    t = d.as_tuple()
    digits = list(t.digits)
    # leading zeros should not exist for non-zero; enforce removal if present
    while digits and digits[0] == 0:
        digits.pop(0)
    if not digits:
        # edge case after normalization
        return b"\x01"
    coeff = 0
    for dig in digits:
        coeff = coeff * 10 + dig
    E = t.exponent  # value = coeff * 10^E
    Eb = E + EXP_BIAS
    vu_Eb = _varuint_be(Eb)
    vu_len = _varuint_be(len(digits))
    # BCD digits, two per byte; pad nibble 0xF if odd
    bcd = bytearray()
    it = iter(digits)
    for i in range(0, len(digits), 2):
        hi = digits[i]
        lo = digits[i + 1] if i + 1 < len(digits) else 0xF
        bcd.append(((hi & 0xF) << 4) | (lo & 0xF))
    if sign_byte == b"\x00":
        vu_Eb = bytes((~x) & 0xFF for x in vu_Eb)
        vu_len = bytes((~x) & 0xFF for x in vu_len)
        bcd = bytes((~x) & 0xFF for x in bcd)
    return sign_byte + vu_Eb + vu_len + bytes(bcd)


def _decode_decimal(b: bytes):
    from decimal import Decimal

    if not b:
        raise ValueError("empty decimal encoding")
    sign = b[0]
    if sign == 0x01:
        return Decimal(0)
    i = 1
    if sign == 0x00:
        # invert streams
        invE = bytearray()
        while True:
            if i >= len(b):
                raise ValueError("truncated decimal")
            invb = (~b[i]) & 0xFF
            invE.append(invb)
            i += 1
            if (invb & 0x80) == 0:
                break
        Eb, _ = _parse_varuint_be(bytes(invE), 0)
        invL = bytearray()
        while True:
            if i >= len(b):
                raise ValueError("truncated decimal len")
            invb = (~b[i]) & 0xFF
            invL.append(invb)
            i += 1
            if (invb & 0x80) == 0:
                break
        L, _ = _parse_varuint_be(bytes(invL), 0)
        # read BCD and invert
        bcd_len = (L + 1) // 2
        bcd = b[i : i + bcd_len]
        if len(bcd) != bcd_len:
            raise ValueError("truncated decimal bcd")
        bcd = bytes((~x) & 0xFF for x in bcd)
        sgn = -1
    elif sign == 0x02:
        Eb, i = _parse_varuint_be(b, i)
        L, i = _parse_varuint_be(b, i)
        bcd_len = (L + 1) // 2
        bcd = b[i : i + bcd_len]
        if len(bcd) != bcd_len:
            raise ValueError("truncated decimal bcd")
        sgn = 1
    else:
        raise ValueError("invalid decimal sign byte")
    E = Eb - EXP_BIAS
    digits = []
    for byte in bcd:
        hi = (byte >> 4) & 0xF
        lo = byte & 0xF
        digits.append(hi)
        digits.append(lo)
    digits = digits[:L]
    coeff = 0
    for dig in digits:
        coeff = coeff * 10 + dig
    val = Decimal(coeff) * (Decimal(10) ** E)
    return -val if sgn < 0 else val


# ---------------- Float32/16/bfloat16 ----------------

def _canonical_float32_bits(x: float) -> int:
    if math.isnan(x):
        return 0x7FC00000
    if x == 0.0:
        return 0x00000000
    return struct.unpack(
        ">I", struct.pack(">f", float(x))
    )[0]


def _float32_to_ordered_bytes(x: float) -> bytes:
    bits = _canonical_float32_bits(x)
    sign = (bits >> 31) & 1
    if sign == 1:
        out = (~bits) & 0xFFFFFFFF
    else:
        out = bits ^ 0x80000000
    return out.to_bytes(4, byteorder="big", signed=False)


def _ordered_bytes_to_float32(b: bytes) -> float:
    if len(b) != 4:
        raise ValueError("expected 4 bytes for float32")
    u = int.from_bytes(b, byteorder="big", signed=False)
    sign = (u >> 31) & 1
    if sign == 1:
        bits = u ^ 0x80000000
    else:
        bits = (~u) & 0xFFFFFFFF
    if bits == 0x7FC00000:
        return float("nan")
    if bits == 0:
        return 0.0
    return struct.unpack(
        ">f", struct.pack(">I", bits)
    )[0]


def _float_to_float16_bits(x: float) -> int:
    # Convert via float32 rounding then to IEEE 754 binary16
    if math.isnan(x):
        return 0x7E00
    if x == 0.0:
        return 0
    # get float32 bits
    f32 = struct.unpack(
        ">I", struct.pack(">f", float(x))
    )[0]
    sign = (f32 >> 31) & 0x1
    exp = (f32 >> 23) & 0xFF
    frac = f32 & 0x7FFFFF
    if exp == 0xFF:
        # inf or nan
        if frac != 0:
            bits = 0x7E00
        else:
            bits = 0x7C00
        return (sign << 15) | bits
    # compute new exponent
    exp16 = exp - 127 + 15
    if exp16 >= 0x1F:
        # overflow -> inf
        return (sign << 15) | 0x7C00
    elif exp16 <= 0:
        # subnormal or zero
        if exp16 < -10:
            return sign << 15
        # mantissa with hidden 1
        mant = frac | 0x00800000
        shift = 1 - exp16
        mant = mant >> shift
        # rounding to nearest even
        mant = mant + 0x00001000
        return (sign << 15) | (mant >> 13)
    else:
        # normal
        mant = frac + 0x00001000
        if mant & 0x00800000:
            mant = 0
            exp16 += 1
            if exp16 >= 0x1F:
                return (sign << 15) | 0x7C00
        return (sign << 15) | ((exp16 & 0x1F) << 10) | ((mant >> 13) & 0x3FF)


def _float16_to_ordered_bytes(x: float) -> bytes:
    bits = _float_to_float16_bits(x)
    # map -0 to +0
    if (bits & 0x7FFF) == 0:
        bits = 0
    sign = (bits >> 15) & 1
    if sign == 1:
        out = (~bits) & 0xFFFF
    else:
        out = bits ^ 0x8000
    return out.to_bytes(2, byteorder="big", signed=False)


def _ordered_bytes_to_float16(b: bytes) -> float:
    if len(b) != 2:
        raise ValueError("expected 2 bytes for float16")
    u = int.from_bytes(b, byteorder="big", signed=False)
    sign = (u >> 15) & 1
    if sign == 1:
        bits = u ^ 0x8000
    else:
        bits = (~u) & 0xFFFF
    if bits == 0x7E00:
        return float("nan")
    if (bits & 0x7FFF) == 0:
        return 0.0
    # expand to float32
    s = (bits >> 15) & 1
    e = (bits >> 10) & 0x1F
    f = bits & 0x3FF
    if e == 0x1F:
        return float("inf") if f == 0 and s == 0 else float("nan")
    if e == 0:
        # subnormal
        val = (f / 1024.0) * (2 ** (-14))
    else:
        val = (1.0 + (f / 1024.0)) * (2 ** (e - 15))
    return -val if s == 1 else val


def _bfloat16_to_ordered_bytes(x: float) -> bytes:
    if math.isnan(x):
        bits16 = 0x7FC0
    else:
        # float32 bits
        f32 = struct.unpack(
            ">I", struct.pack(">f", float(x))
        )[0]
        # map -0 to +0
        if f32 & 0x7FFFFFFF == 0:
            f32 = 0
        # round to nearest even for bf16
        top = f32 >> 16
        low = f32 & 0xFFFF
        # rounding increment
        inc = 1 if (low > 0x8000) or (low == 0x8000 and (top & 1) == 1) else 0
        bits16 = (top + inc) & 0xFFFF
        # normalize NaN to canonical
        if (bits16 & 0x7F80) == 0x7F80 and (bits16 & 0x007F) != 0:
            bits16 = 0x7FC0
    sign = (bits16 >> 15) & 1
    if sign == 1:
        out = (~bits16) & 0xFFFF
    else:
        out = bits16 ^ 0x8000
    return out.to_bytes(2, byteorder="big", signed=False)


def _ordered_bytes_to_bfloat16(b: bytes) -> float:
    if len(b) != 2:
        raise ValueError("expected 2 bytes for bfloat16")
    u = int.from_bytes(b, byteorder="big", signed=False)
    sign = (u >> 15) & 1
    if sign == 1:
        bits = u ^ 0x8000
    else:
        bits = (~u) & 0xFFFF
    if bits == 0x7FC0:
        return float("nan")
    if (bits & 0x7FFF) == 0:
        return 0.0
    # expand to float32 by placing bits as high 16 bits
    f32 = (bits << 16) & 0xFFFFFFFF
    return struct.unpack(
        ">f", struct.pack(
            ">I", f32
        )
    )[0]
