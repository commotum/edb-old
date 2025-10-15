use std::convert::TryInto;

use unicode_normalization::UnicodeNormalization;
use uuid::Uuid;
use num_traits::Zero;

#[derive(Debug, Clone, Copy, PartialEq, Eq, serde::Serialize, serde::Deserialize)]
#[repr(u8)]
pub enum ValueType {
    Long = 1,
    Double = 2,
    Boolean = 3,
    String = 4,
    Keyword = 5,
    Uuid = 6,
    Instant = 7,
    Ref = 8,
    Bytes = 9,
    Uint8 = 10,
    Bigint = 11,
    Decimal = 12,
    Float32 = 13,
    Float16 = 14,
    Bfloat16 = 15,
}

fn varuint_be(mut n: u64) -> Vec<u8> {
    if n == 0 {
        return vec![0u8];
    }
    let mut groups: Vec<u8> = Vec::new();
    while n > 0 {
        groups.push((n & 0x7F) as u8);
        n >>= 7;
    }
    groups.reverse();
    let mut out = Vec::with_capacity(groups.len());
    for (i, g) in groups.iter().enumerate() {
        let last = i == groups.len() - 1;
        if last {
            out.push(*g);
        } else {
            out.push(0x80 | *g);
        }
    }
    out
}

pub fn parse_varuint_be(bytes: &[u8]) -> Result<(u64, usize), String> {
    if bytes.is_empty() {
        return Err("truncated varuint".into());
    }
    let mut val: u64 = 0;
    let mut i = 0usize;
    loop {
        if i >= bytes.len() {
            return Err("truncated varuint".into());
        }
        let b = bytes[i];
        i += 1;
        let cont = (b & 0x80) != 0;
        val = (val << 7) | (b & 0x7F) as u64;
        if !cont {
            break;
        }
    }
    Ok((val, i))
}

fn i64_to_ordered_bytes(x: i64) -> [u8; 8] {
    let u = ((x as i128) + (1i128 << 63)) as u64;
    u.to_be_bytes()
}

fn ordered_bytes_to_i64(b: &[u8]) -> Result<i64, String> {
    if b.len() != 8 {
        return Err("expected 8 bytes for i64".into());
    }
    let u = u64::from_be_bytes(b.try_into().unwrap());
    let x = (u as i128) - (1i128 << 63);
    Ok(x as i64)
}

fn canonical_double_bits(x: f64) -> u64 {
    if x.is_nan() {
        // canonical quiet NaN payload 0
        return 0x7FF8_0000_0000_0000u64;
    }
    // map -0.0 to +0.0
    if x == 0.0 {
        return 0u64;
    }
    x.to_bits()
}

fn double_to_ordered_bytes(x: f64) -> [u8; 8] {
    let bits = canonical_double_bits(x);
    let sign = (bits >> 63) & 1;
    let out = if sign == 1 {
        !bits
    } else {
        bits ^ 0x8000_0000_0000_0000u64
    };
    out.to_be_bytes()
}

fn ordered_bytes_to_double(b: &[u8]) -> Result<f64, String> {
    if b.len() != 8 {
        return Err("expected 8 bytes for double".into());
    }
    let u = u64::from_be_bytes(b.try_into().unwrap());
    let sign = (u >> 63) & 1;
    let bits = if sign == 1 {
        // original non-negative
        u ^ 0x8000_0000_0000_0000u64
    } else {
        !u
    };
    if bits == 0x7FF8_0000_0000_0000u64 {
        return Ok(f64::NAN);
    }
    if bits == 0 {
        return Ok(0.0);
    }
    Ok(f64::from_bits(bits))
}

pub fn encode_scalar_long(x: i64) -> Vec<u8> {
    i64_to_ordered_bytes(x).to_vec()
}

pub fn decode_scalar_long(b: &[u8]) -> Result<i64, String> {
    ordered_bytes_to_i64(b)
}

pub fn encode_scalar_double(x: f64) -> Vec<u8> {
    double_to_ordered_bytes(x).to_vec()
}

pub fn decode_scalar_double(b: &[u8]) -> Result<f64, String> {
    ordered_bytes_to_double(b)
}

pub fn encode_scalar_boolean(x: bool) -> Vec<u8> {
    if x { vec![1u8] } else { vec![0u8] }
}

pub fn decode_scalar_boolean(b: &[u8]) -> Result<bool, String> {
    match b {
        [0] => Ok(false),
        [1] => Ok(true),
        _ => Err("invalid boolean encoding".into()),
    }
}

fn nfc(s: &str) -> String {
    s.nfc().collect()
}

fn unescape_jsonish(s: &str) -> String {
    // Interpret simple JSON-style escapes within an already-parsed string.
    // This is used so vector files can keep ASCII via "\\uXXXX" forms.
    let mut out = String::with_capacity(s.len());
    let mut chars = s.chars().peekable();
    while let Some(c) = chars.next() {
        if c == '\\' {
            match chars.peek().copied() {
                Some('u') => {
                    // consume 'u' and parse 4 hex digits
                    chars.next();
                    let mut hex = String::new();
                    for _ in 0..4 {
                        if let Some(h) = chars.next() { hex.push(h); } else { break; }
                    }
                    if hex.len() == 4 {
                        if let Ok(cp) = u16::from_str_radix(&hex, 16) {
                            if let Some(ch) = std::char::from_u32(cp as u32) {
                                out.push(ch);
                                continue;
                            }
                        }
                    }
                    // fallback: write back literal if malformed
                    out.push('\\'); out.push('u'); out.push_str(&hex);
                }
                Some('n') => { chars.next(); out.push('\n'); }
                Some('r') => { chars.next(); out.push('\r'); }
                Some('t') => { chars.next(); out.push('\t'); }
                Some('\"') => { chars.next(); out.push('"'); }
                Some('\\') => { chars.next(); out.push('\\'); }
                _ => { out.push(c); }
            }
        } else {
            out.push(c);
        }
    }
    out
}

fn parse_i64_or_symbol(s: &str) -> Option<i64> {
    match s {
        "min_i64" => Some(i64::MIN),
        "max_i64" => Some(i64::MAX),
        _ => s.parse::<i64>().ok(),
    }
}

fn parse_rfc3339_us(s: &str) -> Option<i64> {
    // Parse a minimal subset: YYYY-MM-DDTHH:MM:SSZ (UTC only), return milliseconds since Unix epoch
    fn days_from_civil(y: i64, m: u32, d: u32) -> i64 {
        // Howard Hinnant's algorithm (exact transcription)
        let y = y - if m <= 2 { 1 } else { 0 };
        let era = if y >= 0 { y } else { y - 399 } / 400;
        let yoe = (y - era * 400) as i64; // [0, 399]
        let m_adj = m as i64 + if m > 2 { -3 } else { 9 }; // March=0..Feb=11
        let doy = (153 * m_adj + 2) / 5 + d as i64 - 1; // [0, 365]
        let doe = yoe * 365 + yoe / 4 - yoe / 100 + doy; // [0, 146096]
        era * 146097 + doe - 719468
    }
    if s.len() < 20 { return None; }
    // YYYY-MM-DDTHH:MM:SSZ
    if &s[4..5] != "-" || &s[7..8] != "-" || &s[10..11] != "T" || &s[13..14] != ":" || &s[16..17] != ":" { return None; }
    let z = s.as_bytes()[19];
    if z != b'Z' { return None; }
    let year: i64 = s[0..4].parse().ok()?;
    let month: u32 = s[5..7].parse().ok()?;
    let day: u32 = s[8..10].parse().ok()?;
    let hour: i64 = s[11..13].parse().ok()?;
    let min: i64 = s[14..16].parse().ok()?;
    let sec: i64 = s[17..19].parse().ok()?;
    let days = days_from_civil(year, month, day);
    let secs = days * 86_400 + hour * 3600 + min * 60 + sec;
    Some(secs * 1_000_000)
}

fn parse_f64_from_json(value: &serde_json::Value) -> Result<f64, ()> {
    if let Some(n) = value.as_f64() { return Ok(n); }
    if let Some(s) = value.as_str() {
        return Ok(match s {
            "NaN" => f64::NAN,
            "+inf" => f64::INFINITY,
            "-inf" => f64::NEG_INFINITY,
            _ => s.parse::<f64>().map_err(|_| ())?,
        });
    }
    Err(())
}

fn parse_f32_from_json(value: &serde_json::Value) -> Result<f32, ()> {
    if let Some(n) = value.as_f64() { return Ok(n as f32); }
    if let Some(s) = value.as_str() {
        return Ok(match s {
            "NaN" => f32::NAN,
            "+inf" => f32::INFINITY,
            "-inf" => f32::NEG_INFINITY,
            _ => s.parse::<f32>().map_err(|_| ())?,
        });
    }
    Err(())
}

fn parse_bigint_string(s: &str) -> Option<num_bigint::BigInt> {
    use num_bigint::BigInt;
    if let Ok(n) = s.parse::<BigInt>() { return Some(n); }
    // Support simple exponent form like "2^200"
    if let Some((base_s, exp_s)) = s.split_once('^') {
        if let (Ok(base), Ok(exp)) = (base_s.trim().parse::<u64>(), exp_s.trim().parse::<u32>()) {
            use num_traits::Pow;
            let b = BigInt::from(base);
            return Some(b.pow(exp));
        }
    }
    None
}

pub fn encode_scalar_string(s: &str) -> Vec<u8> {
    let s_unescaped = unescape_jsonish(s);
    let s_norm = nfc(&s_unescaped);
    let data = s_norm.into_bytes();
    let mut out = varuint_be(data.len() as u64);
    out.extend_from_slice(&data);
    out
}

pub fn decode_scalar_string(b: &[u8]) -> Result<String, String> {
    let (len, i) = parse_varuint_be(b)?;
    let end = i + (len as usize);
    if end > b.len() {
        return Err("truncated string".into());
    }
    let s = std::str::from_utf8(&b[i..end]).map_err(|_| "invalid utf-8")?;
    Ok(nfc(s))
}

pub fn encode_scalar_keyword(k: &str) -> Result<Vec<u8>, String> {
    if !k.starts_with(':') {
        return Err("KEYWORD must start with ':'".into());
    }
    Ok(encode_scalar_string(k))
}

pub fn decode_scalar_keyword(b: &[u8]) -> Result<String, String> {
    let s = decode_scalar_string(b)?;
    if !s.starts_with(':') {
        return Err("decoded KEYWORD missing ':'".into());
    }
    Ok(s)
}

pub fn encode_scalar_uuid(u: Uuid) -> Vec<u8> { u.into_bytes().to_vec() }

pub fn decode_scalar_uuid(b: &[u8]) -> Result<Uuid, String> {
    if b.len() != 16 { return Err("invalid uuid length".into()); }
    let mut arr = [0u8; 16];
    arr.copy_from_slice(b);
    Ok(Uuid::from_bytes(arr))
}

pub fn encode_scalar_bytes(b: &[u8]) -> Vec<u8> { b.to_vec() }

pub fn decode_scalar_bytes(b: &[u8]) -> Vec<u8> { b.to_vec() }

pub fn encode_scalar_uint8(x: u8) -> Vec<u8> { vec![x] }

pub fn decode_scalar_uint8(b: &[u8]) -> Result<u8, String> {
    if b.len() != 1 { return Err("UINT8 requires 1 byte".into()); }
    Ok(b[0])
}

pub fn encode_scalar(value_type: ValueType, value: &serde_json::Value) -> Result<Vec<u8>, String> {
    use ValueType::*;
    match value_type {
        Long | Ref => {
            let x = if let Some(i) = value.as_i64() { i } else if let Some(s) = value.as_str() { parse_i64_or_symbol(s).ok_or("expected integer")? } else { return Err("expected integer".into()) };
            Ok(encode_scalar_long(x))
        }
        Instant => {
            let x = if let Some(i) = value.as_i64() { i } else if let Some(s) = value.as_str() { parse_i64_or_symbol(s).or_else(|| parse_rfc3339_us(s)).ok_or("expected integer or RFC3339 instant")? } else { return Err("expected integer or RFC3339 instant".into()) };
            Ok(encode_scalar_long(x))
        }
        Double => {
            let x = parse_f64_from_json(value).map_err(|_| "expected number or symbolic double")?;
            Ok(encode_scalar_double(x))
        }
        Float32 => {
            let x = parse_f32_from_json(value).map_err(|_| "bad float32")?;
            Ok(encode_scalar_f32(x))
        }
        Float16 => {
            let x = parse_f32_from_json(value).map_err(|_| "bad float16")?;
            Ok(encode_scalar_f16(x))
        }
        Bfloat16 => {
            let x = parse_f32_from_json(value).map_err(|_| "bad bfloat16")?;
            Ok(encode_scalar_bf16(x))
        }
        Boolean => Ok(encode_scalar_boolean(value.as_bool().ok_or("expected bool")?)),
        String => Ok(encode_scalar_string(value.as_str().ok_or("expected string")?)),
        Keyword => encode_scalar_keyword(value.as_str().ok_or("expected keyword string")?),
        Uuid => {
            let s = value.as_str().ok_or("expected uuid string")?;
            let u = uuid::Uuid::parse_str(s).map_err(|_| "invalid uuid")?;
            Ok(encode_scalar_uuid(u))
        }
        Bytes => Ok(encode_scalar_bytes(value.as_str().ok_or("expected base64? not supported in vectors")?.as_bytes())),
        Uint8 => {
            let x = value.as_u64().ok_or("expected uint")?;
            if x > 255 { return Err("uint8 out of range".into()); }
            Ok(encode_scalar_uint8(x as u8))
        }
        Bigint => encode_scalar_bigint(value),
        Decimal => encode_scalar_decimal(value),
    }
}

pub fn decode_scalar(value_type: ValueType, bytes: &[u8]) -> Result<serde_json::Value, String> {
    use ValueType::*;
    Ok(match value_type {
        Long | Instant | Ref => serde_json::Value::from(decode_scalar_long(bytes)?),
        Double => serde_json::Value::from(decode_scalar_double(bytes)?),
        Float32 => serde_json::Value::from(decode_scalar_f32(bytes)?),
        Float16 => serde_json::Value::from(decode_scalar_f16(bytes)?),
        Bfloat16 => serde_json::Value::from(decode_scalar_bf16(bytes)?),
        Boolean => serde_json::Value::from(decode_scalar_boolean(bytes)?),
        String => serde_json::Value::from(decode_scalar_string(bytes)?),
        Keyword => serde_json::Value::from(decode_scalar_keyword(bytes)?),
        Uuid => serde_json::Value::from(decode_scalar_uuid(bytes).map_err(|e| e.to_string())?.to_string()),
        Bytes => serde_json::Value::from(std::string::String::from_utf8_lossy(bytes).to_string()),
        Uint8 => serde_json::Value::from(decode_scalar_uint8(bytes)? as u64),
        Bigint => serde_json::Value::from(decode_scalar_bigint(bytes)?),
        Decimal => serde_json::Value::from(decode_scalar_decimal(bytes)?),
    })
}

pub fn decode_scalar_with_consumed(value_type: ValueType, bytes: &[u8]) -> Result<(serde_json::Value, usize), String> {
    use ValueType::*;
    match value_type {
        Long | Instant | Ref => Ok((serde_json::Value::from(decode_scalar_long(bytes)?), 8)),
        Double => Ok((serde_json::Value::from(decode_scalar_double(bytes)?), 8)),
        Float32 => Ok((serde_json::Value::from(decode_scalar_f32(bytes)?), 4)),
        Float16 => Ok((serde_json::Value::from(decode_scalar_f16(bytes)?), 2)),
        Bfloat16 => Ok((serde_json::Value::from(decode_scalar_bf16(bytes)?), 2)),
        Boolean => {
            let v = decode_scalar_boolean(bytes)?;
            Ok((serde_json::Value::from(v), 1))
        }
        String | Keyword => {
            let (len, i) = parse_varuint_be(bytes)?;
            let end = i + (len as usize);
            if end > bytes.len() { return Err("truncated string".into()); }
            let val = if let String = value_type { decode_scalar_string(&bytes[..end])? } else { decode_scalar_keyword(&bytes[..end])? };
            Ok((serde_json::Value::from(val), end))
        }
        Uuid => Ok((serde_json::Value::from(decode_scalar_uuid(bytes).map_err(|e| e.to_string())?.to_string()), 16)),
        Bytes => Err("tuple v1 does not support BYTES (no length delimiter)".into()),
        Uint8 => Ok((serde_json::Value::from(decode_scalar_uint8(bytes)? as u64), 1)),
        Bigint => {
            let (s, consumed) = decode_bigint_with_consumed(bytes)?;
            Ok((serde_json::Value::from(s), consumed))
        }
        Decimal => {
            let (s, consumed) = decode_decimal_with_consumed(bytes)?;
            Ok((serde_json::Value::from(s), consumed))
        }
    }
}

// -------- float32/16/bfloat16 --------

fn canonical_f32_bits(x: f32) -> u32 {
    if x.is_nan() { return 0x7FC0_0000u32; }
    if x == 0.0 { return 0; }
    x.to_bits()
}

pub fn encode_scalar_f32(x: f32) -> Vec<u8> {
    let bits = canonical_f32_bits(x);
    let sign = (bits >> 31) & 1;
    let out = if sign == 1 { !bits } else { bits ^ 0x8000_0000u32 };
    out.to_be_bytes().to_vec()
}

pub fn decode_scalar_f32(b: &[u8]) -> Result<f64, String> {
    if b.len() != 4 { return Err("expected 4 bytes".into()); }
    let u = u32::from_be_bytes(b.try_into().unwrap());
    let sign = (u >> 31) & 1;
    let bits = if sign == 1 { u ^ 0x8000_0000u32 } else { !u };
    if bits == 0x7FC0_0000 { return Ok(f64::NAN); }
    if bits == 0 { return Ok(0.0); }
    Ok(f32::from_bits(bits) as f64)
}

pub fn encode_scalar_f16(x: f32) -> Vec<u8> {
    use half::f16;
    let bits: u16 = if x.is_nan() { 0x7E00 } else if x == 0.0 { 0 } else { f16::from_f32(x).to_bits() };
    let bits = if (bits & 0x7FFF) == 0 { 0 } else { bits };
    let sign = (bits >> 15) & 1;
    let out = if sign == 1 { !bits } else { bits ^ 0x8000 };
    out.to_be_bytes().to_vec()
}

pub fn decode_scalar_f16(b: &[u8]) -> Result<f64, String> {
    if b.len() != 2 { return Err("expected 2 bytes".into()); }
    let u = u16::from_be_bytes(b.try_into().unwrap());
    let sign = (u >> 15) & 1;
    let bits = if sign == 1 { u ^ 0x8000 } else { !u };
    if bits == 0x7E00 { return Ok(f64::NAN); }
    if (bits & 0x7FFF) == 0 { return Ok(0.0); }
    use half::f16;
    Ok(f16::from_bits(bits).to_f64())
}

pub fn encode_scalar_bf16(x: f32) -> Vec<u8> {
    use half::bf16;
    let bits: u16 = if x.is_nan() { 0x7FC0 } else { bf16::from_f32(x).to_bits() };
    let bits = if (bits & 0x7FFF) == 0 { 0 } else { bits };
    let sign = (bits >> 15) & 1;
    let out = if sign == 1 { !bits } else { bits ^ 0x8000 };
    out.to_be_bytes().to_vec()
}

pub fn decode_scalar_bf16(b: &[u8]) -> Result<f64, String> {
    if b.len() != 2 { return Err("expected 2 bytes".into()); }
    let u = u16::from_be_bytes(b.try_into().unwrap());
    let sign = (u >> 15) & 1;
    let bits = if sign == 1 { u ^ 0x8000 } else { !u };
    if bits == 0x7FC0 { return Ok(f64::NAN); }
    if (bits & 0x7FFF) == 0 { return Ok(0.0); }
    use half::bf16;
    Ok(bf16::from_bits(bits).to_f32() as f64)
}

// -------- BigInt/Decimal --------

pub fn encode_scalar_bigint(value: &serde_json::Value) -> Result<Vec<u8>, String> {
    use num_bigint::BigInt;
    let n: BigInt = if let Some(i) = value.as_i64() {
        i.into()
    } else if let Some(s) = value.as_str() {
        parse_bigint_string(s).ok_or("invalid bigint")?
    } else {
        return Err("expected int or string".into());
    };
    if n.is_zero() { return Ok(vec![0x01]); }
    let neg = n.sign() == num_bigint::Sign::Minus;
    let mag_bytes = n.magnitude().to_bytes_be();
    let mut out = Vec::new();
    if neg {
        out.push(0x00);
        let vu = varuint_be(mag_bytes.len() as u64);
        out.extend(vu.iter().map(|b| !*b));
        out.extend(mag_bytes.iter().map(|b| !*b));
    } else {
        out.push(0x02);
        out.extend(varuint_be(mag_bytes.len() as u64));
        out.extend_from_slice(&mag_bytes);
    }
    Ok(out)
}

pub fn decode_scalar_bigint(bytes: &[u8]) -> Result<serde_json::Value, String> {
    use num_bigint::BigInt;
    if bytes.is_empty() { return Err("empty bigint".into()); }
    let sign = bytes[0];
    if sign == 0x01 { return Ok(serde_json::Value::from("0")); }
    let mut i = 1usize;
    let (_len, mag, neg) = if sign == 0x02 {
        let (l, ni) = parse_varuint_be(&bytes[i..]).map_err(|e| e.to_string())?;
        i += ni;
        let len_bytes = l as usize;
        let end = i + len_bytes;
        if end > bytes.len() { return Err("truncated magnitude".into()); }
        (len_bytes, bytes[i..end].to_vec(), false)
    } else if sign == 0x00 {
        // invert varuint then magnitude
        let mut inv = Vec::new();
        loop {
            if i >= bytes.len() { return Err("truncated varuint".into()); }
            let invb = !bytes[i];
            inv.push(invb);
            i += 1;
            if (invb & 0x80) == 0 { break; }
        }
        let (l, _) = parse_varuint_be(&inv).map_err(|e| e.to_string())?;
        let len_bytes = l as usize;
        let end = i + len_bytes;
        if end > bytes.len() { return Err("truncated magnitude".into()); }
        let mag_inv = &bytes[i..end];
        let mag: Vec<u8> = mag_inv.iter().map(|b| !*b).collect();
        (len_bytes, mag, true)
    } else {
        return Err("invalid sign byte".into());
    };
    let n = BigInt::from_bytes_be(num_bigint::Sign::Plus, &mag);
    let n = if neg { -n } else { n };
    Ok(serde_json::Value::from(n.to_string()))
}

pub fn decode_bigint_with_consumed(bytes: &[u8]) -> Result<(String, usize), String> {
    use num_bigint::BigInt;
    if bytes.is_empty() { return Err("empty bigint".into()); }
    let sign = bytes[0];
    if sign == 0x01 { return Ok(("0".to_string(), 1)); }
    let mut i = 1usize;
    let (len, mag, neg) = if sign == 0x02 {
        let (l, ni) = parse_varuint_be(&bytes[i..]).map_err(|e| e.to_string())?;
        i += ni;
        let len_bytes = l as usize;
        let end = i + len_bytes;
        if end > bytes.len() { return Err("truncated magnitude".into()); }
        (len_bytes, bytes[i..end].to_vec(), false)
    } else if sign == 0x00 {
        let mut inv = Vec::new();
        loop {
            if i >= bytes.len() { return Err("truncated varuint".into()); }
            let invb = !bytes[i];
            inv.push(invb);
            i += 1;
            if (invb & 0x80) == 0 { break; }
        }
        let (l, _) = parse_varuint_be(&inv).map_err(|e| e.to_string())?;
        let len_bytes = l as usize;
        let end = i + len_bytes;
        if end > bytes.len() { return Err("truncated magnitude".into()); }
        let mag_inv = &bytes[i..end];
        let mag: Vec<u8> = mag_inv.iter().map(|b| !*b).collect();
        (len_bytes, mag, true)
    } else {
        return Err("invalid sign byte".into());
    };
    let n = BigInt::from_bytes_be(num_bigint::Sign::Plus, &mag);
    let n = if neg { -n } else { n };
    Ok((n.to_string(), 1 + (if sign == 0x01 { 0 } else { if sign == 0x02 { varuint_be(len as u64).len() } else { let mut tmp = Vec::new(); let mut j = 1usize; loop { let invb = !bytes[j]; tmp.push(invb); j += 1; if (invb & 0x80) == 0 { break; } } parse_varuint_be(&tmp).unwrap().1 } }) + len))
}

pub fn encode_scalar_decimal(value: &serde_json::Value) -> Result<Vec<u8>, String> {
    use bigdecimal::BigDecimal;
    const EXP_BIAS: i64 = 1 << 20;
    let s = if value.is_string() { value.as_str().unwrap().to_string() } else { value.to_string() };
    let mut d: BigDecimal = s.parse().map_err(|_| "invalid decimal")?;
    if d.is_zero() { return Ok(vec![0x01]); }
    // normalize and obtain (coefficient, scale) where value = coeff * 10^{-scale}
    d = d.normalized();
    let (coeff, mut scale) = d.as_bigint_and_exponent();
    let neg = coeff.sign() == num_bigint::Sign::Minus;
    // Build decimal digits from absolute coeff then trim trailing zeros, adjusting scale
    let mut digits_str = coeff.magnitude().to_str_radix(10);
    if digits_str.is_empty() { return Ok(vec![0x01]); }
    let mut trimmed = 0usize;
    while digits_str.ends_with('0') {
        digits_str.pop();
        trimmed += 1;
    }
    if trimmed > 0 { scale -= trimmed as i64; }
    let mut out = Vec::new();
    if neg { out.push(0x00); } else { out.push(0x02); }
    // E = -scale so that value = digits * 10^E
    let exp = -(scale as i128);
    let eb = (exp + (EXP_BIAS as i128)) as u64;
    let vu_e = varuint_be(eb);
    // build BCD from digits_str
    let digits: Vec<u8> = digits_str.bytes().map(|ch| (ch - b'0') as u8).collect();
    let vu_len = varuint_be(digits.len() as u64);
    let mut bcd: Vec<u8> = Vec::with_capacity((digits.len()+1)/2);
    for i in (0..digits.len()).step_by(2) {
        let hi = digits[i] & 0xF;
        let lo = if i+1 < digits.len() { digits[i+1] & 0xF } else { 0xF };
        bcd.push((hi << 4) | lo);
    }
    if neg {
        out.extend(vu_e.iter().map(|b| !*b));
        out.extend(vu_len.iter().map(|b| !*b));
        out.extend(bcd.iter().map(|b| !*b));
    } else {
        out.extend(vu_e);
        out.extend(vu_len);
        out.extend(bcd);
    }
    Ok(out)
}

pub fn decode_scalar_decimal(bytes: &[u8]) -> Result<serde_json::Value, String> {
    use bigdecimal::BigDecimal;
    use num_bigint::BigInt;
    const EXP_BIAS: i64 = 1 << 20;
    if bytes.is_empty() { return Err("empty decimal".into()); }
    let sign = bytes[0];
    if sign == 0x01 { return Ok(serde_json::Value::from("0")); }
    let mut i = 1usize;
    let (eb_i64, l_digits, bcd, neg) = if sign == 0x02 {
        let (eb, ni) = parse_varuint_be(&bytes[i..]).map_err(|e| e.to_string())?; i += ni;
        let (l, nj) = parse_varuint_be(&bytes[i..]).map_err(|e| e.to_string())?; i += nj;
        let bcd_len = ((l as usize) + 1) / 2;
        let end = i + bcd_len; if end > bytes.len() { return Err("truncated bcd".into()); }
        (eb as i64, l as usize, bytes[i..end].to_vec(), false)
    } else if sign == 0x00 {
        let mut inv_e = Vec::new();
        loop { if i >= bytes.len() { return Err("truncated e".into()); } let invb = !bytes[i]; inv_e.push(invb); i += 1; if (invb & 0x80) == 0 { break; } }
        let (eb, _) = parse_varuint_be(&inv_e).map_err(|e| e.to_string())?;
        let mut inv_l = Vec::new();
        loop { if i >= bytes.len() { return Err("truncated l".into()); } let invb = !bytes[i]; inv_l.push(invb); i += 1; if (invb & 0x80) == 0 { break; } }
        let (l, _) = parse_varuint_be(&inv_l).map_err(|e| e.to_string())?;
        let bcd_len = ((l as usize) + 1) / 2; let end = i + bcd_len; if end > bytes.len() { return Err("truncated bcd".into()); }
        let bcd_inv = &bytes[i..end];
        let bcd: Vec<u8> = bcd_inv.iter().map(|b| !*b).collect();
        (eb as i64, l as usize, bcd, true)
    } else { return Err("invalid sign".into()); };
    let exp = eb_i64 - EXP_BIAS;
    // reconstruct digits
    let mut digits: Vec<u8> = Vec::with_capacity(l_digits);
    for byte in bcd { digits.push((byte >> 4) & 0xF); digits.push(byte & 0xF); }
    digits.truncate(l_digits);
    // build coefficient BigInt from digits
    let mut coeff = BigInt::from(0); let ten = BigInt::from(10);
    for d in digits { coeff = coeff * &ten + BigInt::from(d as i32); }
    if neg { coeff = -coeff; }
    let bd = if exp >= 0 { let pow = BigInt::from(10).pow(exp as u32); BigDecimal::new(coeff * pow, 0) } else { BigDecimal::new(coeff, (-exp) as i64) };
    Ok(serde_json::Value::from(bd.to_string()))
}

pub fn decode_decimal_with_consumed(bytes: &[u8]) -> Result<(String, usize), String> {
    if bytes.is_empty() { return Err("empty decimal".into()); }
    let sign = bytes[0];
    if sign == 0x01 { return Ok(("0".to_string(), 1)); }
    let mut i = 1usize;
    let consumed = if sign == 0x02 {
        let (_eb, ni) = parse_varuint_be(&bytes[i..]).map_err(|e| e.to_string())?; i += ni;
        let (l, nj) = parse_varuint_be(&bytes[i..]).map_err(|e| e.to_string())?; i += nj;
        let bcd_len = ((l as usize) + 1) / 2; i + bcd_len
    } else if sign == 0x00 {
        // invert count
        // count inv varuint for E
        loop { if i >= bytes.len() { return Err("truncated e".into()); } let invb = !bytes[i]; i += 1; if (invb & 0x80) == 0 { break; } }
        // count inv varuint for L
        loop { if i >= bytes.len() { return Err("truncated l".into()); } let invb = !bytes[i]; i += 1; if (invb & 0x80) == 0 { break; } }
        // cannot know L without parsing inv; recompute properly
        let mut idx = 1usize; let mut inv_e = Vec::new();
        loop { let invb = !bytes[idx]; inv_e.push(invb); idx += 1; if (invb & 0x80) == 0 { break; } }
        let (_eb, _) = parse_varuint_be(&inv_e).map_err(|e| e.to_string())?;
        let mut inv_l = Vec::new();
        loop { let invb = !bytes[idx]; inv_l.push(invb); idx += 1; if (invb & 0x80) == 0 { break; } }
        let (l, _) = parse_varuint_be(&inv_l).map_err(|e| e.to_string())?;
        let bcd_len = ((l as usize) + 1) / 2; idx + bcd_len
    } else { return Err("invalid sign".into()); };
    let val = decode_scalar_decimal(bytes)?;
    Ok((val.as_str().unwrap().to_string(), consumed))
}
