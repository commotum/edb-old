//! Explicit conversion between EDN data and Atomic's narrower value domains.
//!
//! Parsing EDN does not imply that a value can be stored as a datom. In
//! particular nil, characters, maps and sets are not stored scalar types.
//! Native-only value types use namespaced tags; these never execute code.

use crate::edn::EdnValue;
use crate::{
    Keyword, QueryResult, QueryValue, ReturnMapShape, ReturnMaps, SemanticError, Symbol, Value,
};

const MAX_DEPTH: usize = 64;
const MAX_NODES: usize = 1_000_000;
const MAX_BYTES: usize = 64 * 1024 * 1024;

struct Budget {
    nodes: usize,
    bytes: usize,
}
impl Budget {
    fn new() -> Self {
        Self {
            nodes: MAX_NODES,
            bytes: MAX_BYTES,
        }
    }
    fn node(&mut self, depth: usize) -> Result<(), SemanticError> {
        if depth > MAX_DEPTH || self.nodes == 0 {
            return Err(limit());
        }
        self.nodes -= 1;
        self.bytes(std::mem::size_of::<EdnValue>())
    }
    fn bytes(&mut self, bytes: usize) -> Result<(), SemanticError> {
        self.bytes = self.bytes.checked_sub(bytes).ok_or_else(limit)?;
        Ok(())
    }
}
fn limit() -> SemanticError {
    SemanticError::incorrect(
        "edn/conversion-limit",
        "value conversion exceeds its depth, node or accounted-byte limit",
    )
}
fn invalid(message: &str) -> SemanticError {
    SemanticError::incorrect("edn/value-type", message)
}
fn tag(name: &str, value: EdnValue) -> EdnValue {
    let symbol = match name.split_once('/') {
        Some((namespace, name)) => Symbol::new(namespace, name),
        None => Symbol::unqualified(name),
    };
    EdnValue::Tagged(symbol, Box::new(value))
}
fn tag_text(name: &str, value: String) -> EdnValue {
    tag(name, EdnValue::String(value))
}

/// Convert EDN to a stored value, without schema inference. Sequences denote
/// tuple values here; transaction adapters must distinguish many-values and
/// lookup references before calling this function.
pub fn edn_to_value(value: &EdnValue) -> Result<Value, SemanticError> {
    from_edn(value, &mut Budget::new(), 0)
}

fn from_edn(value: &EdnValue, budget: &mut Budget, depth: usize) -> Result<Value, SemanticError> {
    budget.node(depth)?;
    Ok(match value {
        EdnValue::Bool(v) => Value::Bool(*v),
        EdnValue::Long(v) => Value::Long(*v),
        EdnValue::Double(v) => Value::Double(*v),
        EdnValue::BigInt(v) => {
            budget.bytes(v.bits().div_ceil(8) as usize)?;
            Value::BigInt(v.clone())
        }
        EdnValue::BigDec(v) => {
            budget.bytes(v.digits() as usize)?;
            Value::BigDec(v.clone())
        }
        EdnValue::String(v) => {
            budget.bytes(v.len())?;
            Value::String(v.clone())
        }
        EdnValue::Keyword(v) => {
            budget.bytes(v.name.len() + v.namespace.as_ref().map_or(0, String::len))?;
            Value::Keyword(v.clone())
        }
        EdnValue::Symbol(v) => {
            budget.bytes(v.name.len() + v.namespace.as_ref().map_or(0, String::len))?;
            Value::Symbol(v.clone())
        }
        EdnValue::Vector(values) | EdnValue::List(values) => {
            if !(2..=8).contains(&values.len()) {
                return Err(invalid("stored tuples require 2–8 slots"));
            }
            let mut slots = Vec::with_capacity(values.len());
            for value in values {
                if matches!(value, EdnValue::Nil) {
                    budget.node(depth + 1)?;
                    slots.push(None);
                } else {
                    slots.push(Some(from_edn(value, budget, depth + 1)?));
                }
            }
            Value::Tuple(slots)
        }
        EdnValue::Tagged(name, payload) => {
            budget.node(depth + 1)?;
            let text = match payload.as_ref() {
                EdnValue::String(s) => Some(s.as_str()),
                _ => None,
            };
            if let Some(text) = text {
                budget.bytes(text.len())?;
            }
            match (name.namespace.as_deref(), name.name.as_str()) {
                (None, "uuid") => Value::Uuid(parse_uuid(
                    text.ok_or_else(|| invalid("#uuid requires a string"))?,
                )?),
                (None, "inst") => Value::Instant(instant_millis(
                    text.ok_or_else(|| invalid("#inst requires a string"))?,
                )?),
                (Some("atomic"), "instant-millis") => match payload.as_ref() {
                    EdnValue::Long(v) => Value::Instant(*v),
                    _ => {
                        return Err(invalid(
                            "#atomic/instant-millis requires a signed 64-bit integer",
                        ));
                    }
                },
                (Some("atomic"), "ref") => {
                    let reference = match payload.as_ref() {
                        EdnValue::Long(v) => u64::try_from(*v).ok(),
                        EdnValue::BigInt(v) => num_traits::ToPrimitive::to_u64(v),
                        _ => None,
                    }
                    .ok_or_else(|| {
                        invalid("#atomic/ref requires a nonnegative supported entity ID")
                    })?;
                    if reference > crate::MAX_EID {
                        return Err(invalid("reference exceeds the supported entity ID range"));
                    }
                    Value::Ref(reference)
                }
                (Some("atomic"), "uri") => Value::Uri(
                    text.ok_or_else(|| invalid("#atomic/uri requires a string"))?
                        .to_owned(),
                ),
                (Some("atomic"), "bytes") => {
                    Value::Bytes(decode_hex(text.ok_or_else(|| {
                        invalid("#atomic/bytes requires a hexadecimal string")
                    })?)?)
                }
                (Some("atomic"), "function") => {
                    let bytes = decode_hex(text.ok_or_else(|| {
                        invalid("#atomic/function requires a hexadecimal content hash")
                    })?)?;
                    Value::Function(
                        bytes
                            .try_into()
                            .map_err(|_| invalid("function content hash must contain 32 bytes"))?,
                    )
                }
                (Some("atomic"), "float") => {
                    let v = match payload.as_ref() {
                        EdnValue::String(v) => v
                            .parse::<f32>()
                            .map_err(|_| invalid("invalid #atomic/float value"))?,
                        EdnValue::Double(v) => *v as f32,
                        _ => {
                            return Err(invalid(
                                "#atomic/float requires a floating-point literal or string",
                            ));
                        }
                    };
                    if !v.is_finite() {
                        return Err(invalid("nonfinite float requires #atomic/float-bits"));
                    }
                    Value::Float(v)
                }
                (Some("atomic"), "float-bits") => {
                    let bytes: [u8; 4] = decode_hex(
                        text.ok_or_else(|| invalid("float bits require a hexadecimal string"))?,
                    )?
                    .try_into()
                    .map_err(|_| invalid("float bits require four bytes"))?;
                    Value::Float(f32::from_bits(u32::from_be_bytes(bytes)))
                }
                (Some("atomic"), "double-bits") => {
                    let bytes: [u8; 8] = decode_hex(
                        text.ok_or_else(|| invalid("double bits require a hexadecimal string"))?,
                    )?
                    .try_into()
                    .map_err(|_| invalid("double bits require eight bytes"))?;
                    Value::Double(f64::from_bits(u64::from_be_bytes(bytes)))
                }
                _ => {
                    return Err(invalid(
                        "tag is not a registered Atomic stored-value representation",
                    ));
                }
            }
        }
        EdnValue::Nil | EdnValue::Char(_) | EdnValue::Map(_) | EdnValue::Set(_) => {
            return Err(invalid(
                "nil, characters, maps and sets are not stored scalar values",
            ));
        }
    })
}

/// Convert a stored value into readable EDN, preserving its native type. Ref,
/// Float, Bytes, URI, Function and out-of-RFC3339-range instants use atomic/*
/// tags. Nonfinite binary floats retain their exact bits, including NaN payloads.
pub fn value_to_edn(value: &Value) -> Result<EdnValue, SemanticError> {
    to_edn(value, &mut Budget::new(), 0)
}
fn to_edn(value: &Value, budget: &mut Budget, depth: usize) -> Result<EdnValue, SemanticError> {
    budget.node(depth)?;
    Ok(match value {
        Value::Bool(v) => EdnValue::Bool(*v),
        Value::Long(v) => EdnValue::Long(*v),
        Value::BigInt(v) => {
            budget.bytes(v.bits().div_ceil(8) as usize)?;
            EdnValue::BigInt(v.clone())
        }
        Value::BigDec(v) => {
            budget.bytes(v.digits() as usize)?;
            EdnValue::BigDec(v.clone())
        }
        Value::Double(v) if v.is_finite() => EdnValue::Double(*v),
        Value::Double(v) => tag_text("atomic/double-bits", hex(&v.to_bits().to_be_bytes())),
        Value::Float(v) if v.is_finite() => tag_text("atomic/float", v.to_string()),
        Value::Float(v) => tag_text("atomic/float-bits", hex(&v.to_bits().to_be_bytes())),
        Value::String(v) => {
            budget.bytes(v.len())?;
            EdnValue::String(v.clone())
        }
        Value::Keyword(v) => {
            budget.bytes(v.name.len() + v.namespace.as_ref().map_or(0, String::len))?;
            EdnValue::Keyword(v.clone())
        }
        Value::Symbol(v) => {
            budget.bytes(v.name.len() + v.namespace.as_ref().map_or(0, String::len))?;
            EdnValue::Symbol(v.clone())
        }
        Value::Ref(v) => tag(
            "atomic/ref",
            if let Ok(v) = i64::try_from(*v) {
                EdnValue::Long(v)
            } else {
                EdnValue::BigInt((*v).into())
            },
        ),
        Value::Instant(v) => match crate::edn::instant::format_millis(*v) {
            Some(v) => tag_text("inst", v),
            None => tag("atomic/instant-millis", EdnValue::Long(*v)),
        },
        Value::Uuid(v) => {
            let bytes = hex(&v.to_be_bytes());
            tag_text(
                "uuid",
                format!(
                    "{}-{}-{}-{}-{}",
                    &bytes[..8],
                    &bytes[8..12],
                    &bytes[12..16],
                    &bytes[16..20],
                    &bytes[20..]
                ),
            )
        }
        Value::Uri(v) => {
            budget.bytes(v.len())?;
            tag_text("atomic/uri", v.clone())
        }
        Value::Bytes(v) => {
            budget.bytes(v.len().checked_mul(2).ok_or_else(limit)?)?;
            tag_text("atomic/bytes", hex(v))
        }
        Value::Function(v) => tag_text("atomic/function", hex(v)),
        Value::Tuple(v) => {
            let mut values = Vec::new();
            for value in v {
                values.push(match value {
                    None => {
                        budget.node(depth + 1)?;
                        EdnValue::Nil
                    }
                    Some(value) => to_edn(value, budget, depth + 1)?,
                });
            }
            EdnValue::Vector(values)
        }
    })
}

/// Convert general EDN data into the query-only value domain. This does not
/// manufacture new stored scalar types for unsupported EDN characters/tags.
pub fn edn_to_query_value(value: &EdnValue) -> Result<QueryValue, SemanticError> {
    to_query(value, &mut Budget::new(), 0)
}
fn to_query(
    value: &EdnValue,
    budget: &mut Budget,
    depth: usize,
) -> Result<QueryValue, SemanticError> {
    budget.node(depth)?;
    Ok(match value {
        EdnValue::Nil => QueryValue::Nil,
        EdnValue::List(v) | EdnValue::Vector(v) => QueryValue::Tuple(
            v.iter()
                .map(|v| to_query(v, budget, depth + 1))
                .collect::<Result<_, _>>()?,
        ),
        EdnValue::Set(v) => QueryValue::Collection(
            v.iter()
                .map(|v| to_query(v, budget, depth + 1))
                .collect::<Result<_, _>>()?,
        ),
        EdnValue::Map(v) => {
            let mut entries = v
                .iter()
                .map(|(k, v)| {
                    Ok((
                        to_query(k, budget, depth + 1)?,
                        to_query(v, budget, depth + 1)?,
                    ))
                })
                .collect::<Result<Vec<_>, SemanticError>>()?;
            entries.sort_by(|(left, _), (right, _)| left.canonical_cmp(right));
            if entries
                .windows(2)
                .any(|pair| pair[0].0.canonical_cmp(&pair[1].0).is_eq())
            {
                return Err(SemanticError::incorrect(
                    "edn/query-key-collision",
                    "distinct EDN keys collapse under native query-value equality",
                ));
            }
            QueryValue::Map(entries)
        }
        _ => QueryValue::Scalar(from_edn(value, budget, depth)?),
    })
}

/// Encode a query/pull value without flattening nested maps or nil values.
pub fn query_value_to_edn(value: &QueryValue) -> Result<EdnValue, SemanticError> {
    from_query(value, &mut Budget::new(), 0)
}
fn from_query(
    value: &QueryValue,
    budget: &mut Budget,
    depth: usize,
) -> Result<EdnValue, SemanticError> {
    budget.node(depth)?;
    Ok(match value {
        QueryValue::Nil => EdnValue::Nil,
        QueryValue::Scalar(v) => to_edn(v, budget, depth)?,
        QueryValue::Collection(v) | QueryValue::Tuple(v) => EdnValue::Vector(
            v.iter()
                .map(|v| from_query(v, budget, depth + 1))
                .collect::<Result<_, _>>()?,
        ),
        QueryValue::Map(v) => EdnValue::Map(
            v.iter()
                .map(|(k, v)| {
                    Ok((
                        from_query(k, budget, depth + 1)?,
                        from_query(v, budget, depth + 1)?,
                    ))
                })
                .collect::<Result<_, SemanticError>>()?,
        ),
    })
}

/// Relation results are EDN sets of tuples; collection/tuple find results are
/// vectors, and missing scalar/tuple results are nil.
pub fn query_result_to_edn(result: &QueryResult) -> Result<EdnValue, SemanticError> {
    let budget = &mut Budget::new();
    Ok(match result {
        QueryResult::Relation(rows) => EdnValue::Set(
            rows.iter()
                .map(|row| {
                    budget.node(0)?;
                    Ok(EdnValue::Vector(
                        row.iter()
                            .map(|v| from_query(v, budget, 1))
                            .collect::<Result<_, _>>()?,
                    ))
                })
                .collect::<Result<_, SemanticError>>()?,
        ),
        QueryResult::Collection(v) | QueryResult::Tuple(Some(v)) => EdnValue::Vector(
            v.iter()
                .map(|v| from_query(v, budget, 1))
                .collect::<Result<_, _>>()?,
        ),
        QueryResult::Scalar(Some(v)) => from_query(v, budget, 0)?,
        QueryResult::Scalar(None) | QueryResult::Tuple(None) => EdnValue::Nil,
    })
}

/// Preserve keyword/string/symbol return-map keys and the original find shape.
pub fn return_maps_to_edn(result: &ReturnMaps) -> Result<EdnValue, SemanticError> {
    let budget = &mut Budget::new();
    let rows = result
        .iter()
        .map(|row| {
            budget.node(0)?;
            Ok(EdnValue::Map(
                row.iter()
                    .map(|(key, value)| {
                        Ok((to_edn(key, budget, 1)?, from_query(value, budget, 1)?))
                    })
                    .collect::<Result<_, SemanticError>>()?,
            ))
        })
        .collect::<Result<Vec<_>, SemanticError>>()?;
    Ok(match result.shape() {
        ReturnMapShape::Relation => EdnValue::Set(rows),
        ReturnMapShape::Tuple => rows.into_iter().next().unwrap_or(EdnValue::Nil),
    })
}

fn parse_uuid(text: &str) -> Result<u128, SemanticError> {
    if text.len() != 36 || [8, 13, 18, 23].iter().any(|&i| text.as_bytes()[i] != b'-') {
        return Err(invalid(
            "UUID requires canonical 8-4-4-4-12 hexadecimal groups",
        ));
    }
    let compact: String = text.chars().filter(|&c| c != '-').collect();
    let bytes: [u8; 16] = decode_hex(&compact)?
        .try_into()
        .map_err(|_| invalid("invalid UUID"))?;
    Ok(u128::from_be_bytes(bytes))
}
fn instant_millis(text: &str) -> Result<i64, SemanticError> {
    let parts = crate::edn::instant::parse_instant(text)?;
    if parts.leap_second || parts.fraction.bytes().skip(3).any(|b| b != b'0') {
        return Err(SemanticError::incorrect(
            "edn/instant-precision",
            "stored instants have millisecond precision and cannot represent leap seconds",
        ));
    }
    let mut millis = 0i64;
    let mut digits = parts.fraction.bytes();
    for _ in 0..3 {
        millis = millis * 10 + i64::from(digits.next().unwrap_or(b'0') - b'0');
    }
    parts
        .seconds
        .checked_mul(1000)
        .and_then(|v| v.checked_add(millis))
        .ok_or_else(|| invalid("instant exceeds stored range"))
}
fn hex(bytes: &[u8]) -> String {
    const DIGITS: &[u8; 16] = b"0123456789abcdef";
    let mut output = String::with_capacity(bytes.len() * 2);
    for &byte in bytes {
        output.push(char::from(DIGITS[(byte >> 4) as usize]));
        output.push(char::from(DIGITS[(byte & 15) as usize]));
    }
    output
}
fn decode_hex(text: &str) -> Result<Vec<u8>, SemanticError> {
    if !text.len().is_multiple_of(2) {
        return Err(invalid("hexadecimal data must contain complete bytes"));
    }
    text.as_bytes()
        .chunks_exact(2)
        .map(|pair| {
            let high = char::from(pair[0])
                .to_digit(16)
                .ok_or_else(|| invalid("invalid hexadecimal data"))?;
            let low = char::from(pair[1])
                .to_digit(16)
                .ok_or_else(|| invalid("invalid hexadecimal data"))?;
            Ok((high * 16 + low) as u8)
        })
        .collect()
}

/// Convenience constructor for metadata/result maps used by text applications.
pub fn edn_keyword(namespace: &str, name: &str) -> EdnValue {
    EdnValue::Keyword(Keyword::new(namespace, name))
}
