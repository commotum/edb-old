use crate::scalar::{decode_scalar_with_consumed, encode_scalar_double, encode_scalar_long, encode_scalar_uint8, encode_scalar_uuid, ValueType};

const TT_TUPLE: u8 = 0xF1;
const TUPLE_VER: u8 = 0x01;

pub fn encode_tuple_v1(values: &[serde_json::Value], elem_type: ValueType) -> Result<Vec<u8>, String> {
    if values.is_empty() { return Err("tuple arity must be >= 1".into()); }
    let arity = values.len();
    if arity > 255 { return Err("tuple arity too large for v1".into()); }
    let mut out = Vec::with_capacity(3 + arity + 16 * arity);
    out.push(TT_TUPLE);
    out.push(TUPLE_VER);
    out.push(arity as u8);
    let types: Vec<u8> = vec![elem_type as u8; arity];
    out.extend_from_slice(&types);
    for v in values {
        match elem_type {
            ValueType::Long | ValueType::Instant | ValueType::Ref => {
                let x = v.as_i64().ok_or("expected integer")?;
                out.extend_from_slice(&encode_scalar_long(x));
            }
            ValueType::Double => {
                let x = if v.is_number() { v.as_f64().unwrap() } else if v == "NaN" { f64::NAN } else if v == "+inf" { f64::INFINITY } else if v == "-inf" { f64::NEG_INFINITY } else { return Err("bad double".into()) };
                out.extend_from_slice(&encode_scalar_double(x));
            }
            ValueType::Uint8 => {
                let x = v.as_u64().ok_or("expected uint")?;
                if x > 255 { return Err("uint8 out of range".into()); }
                out.extend_from_slice(&encode_scalar_uint8(x as u8));
            }
            ValueType::Uuid => {
                let s = v.as_str().ok_or("expected uuid string")?;
                let u = uuid::Uuid::parse_str(s).map_err(|_| "invalid uuid")?;
                out.extend_from_slice(&encode_scalar_uuid(u));
            }
            ValueType::String => {
                let s = v.as_str().ok_or("expected string")?;
                out.extend_from_slice(&crate::scalar::encode_scalar_string(s));
            }
            _ => return Err("MVP tuple decode supports fixed-width types only".into()),
        }
    }
    Ok(out)
}

pub fn tuple_order_bytes(tuple_bytes: &[u8]) -> Result<&[u8], String> {
    if tuple_bytes.len() < 3 { return Err("invalid tuple header".into()); }
    if tuple_bytes[0] != TT_TUPLE || tuple_bytes[1] != TUPLE_VER { return Err("unsupported tuple encoding".into()); }
    let arity = tuple_bytes[2] as usize;
    let offset = 3 + arity;
    if offset > tuple_bytes.len() { return Err("truncated tuple header".into()); }
    Ok(&tuple_bytes[offset..])
}

pub fn decode_tuple_v1(tuple_bytes: &[u8]) -> Result<(ValueType, Vec<serde_json::Value>), String> {
    if tuple_bytes.len() < 3 { return Err("invalid tuple header".into()); }
    if tuple_bytes[0] != TT_TUPLE || tuple_bytes[1] != TUPLE_VER { return Err("unsupported tuple encoding".into()); }
    let arity = tuple_bytes[2] as usize;
    if tuple_bytes.len() < 3 + arity { return Err("truncated tuple header".into()); }
    let type_code = tuple_bytes[3];
    let vt = match type_code {
        1 => ValueType::Long,
        2 => ValueType::Double,
        6 => ValueType::Uuid,
        7 => ValueType::Instant,
        8 => ValueType::Ref,
        10 => ValueType::Uint8,
        _ => return Err("unsupported tuple elem type".into()),
    };
    let payload = tuple_order_bytes(tuple_bytes)?;
    let mut out: Vec<serde_json::Value> = Vec::with_capacity(arity);
    let mut offset = 0usize;
    for _ in 0..arity {
        if offset >= payload.len() { return Err("tuple payload truncated".into()); }
        let (val, consumed) = decode_scalar_with_consumed(vt, &payload[offset..])?;
        out.push(val);
        offset += consumed;
    }
    if offset != payload.len() { return Err("tuple payload length mismatch".into()); }
    Ok((vt, out))
}
