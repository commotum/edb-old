use serde_json::Value as J;

use crate::allocator::{EntidAllocator, TempResolver};
use crate::model::{EntityRef, TempId, TxOp, Value};
use crate::traits::DbView;
use edb_encoding::ValueType;

fn parse_entity_ref(j: &J) -> EntityRef {
    if let Some(n) = j.as_i64() {
        EntityRef::Entid(n)
    } else if let Some(s) = j.as_str() {
        if let Some(rest) = s.strip_prefix("temp:") {
            EntityRef::TempId(TempId(rest.to_string()))
        } else {
            // Treat bare strings as tempids for simplicity
            EntityRef::TempId(TempId(s.to_string()))
        }
    } else if let Some(arr) = j.as_array() {
        if arr.len() == 3 && arr[0] == J::String("lookup".to_string()) {
            let a = arr[1].as_str().unwrap_or("").to_string();
            let vj = arr[2].clone();
            let v = parse_scalar_value(&vj);
            EntityRef::LookupRef { attr: a, value: v }
        } else {
            EntityRef::TempId(TempId(format!("unhandled:{:?}", j)))
        }
    } else {
        EntityRef::TempId(TempId(format!("unhandled:{:?}", j)))
    }
}

fn parse_scalar_value(j: &J) -> Value {
    if let Some(b) = j.as_bool() { Value::Boolean(b) }
    else if let Some(i) = j.as_i64() { Value::Long(i) }
    else if let Some(f) = j.as_f64() { Value::Double(f) }
    else if let Some(s) = j.as_str() {
        if s.starts_with(':') { Value::Keyword(s.to_string()) } else { Value::String(s.to_string()) }
    } else { Value::String(j.to_string()) }
}

fn value_from_json_for_type(
    db: &dyn DbView,
    temps: &mut TempResolver,
    alloc: &mut dyn EntidAllocator,
    vt: ValueType,
    j: &J,
) -> Value {
    match vt {
        ValueType::Ref => {
            let er = parse_entity_ref(j);
            match er {
                EntityRef::Entid(e) => Value::Ref(e),
                EntityRef::TempId(t) => Value::Ref(temps.resolve_or_alloc(&t, alloc)),
                EntityRef::LookupRef { attr, value } => {
                    if let Some(eid) = db.lookup_by_unique(&attr, &value) { Value::Ref(eid) } else { Value::Ref(temps.resolve_or_alloc(&TempId(format!("lkup:{}", attr)), alloc)) }
                }
            }
        }
        ValueType::Long => Value::Long(j.as_i64().unwrap_or(0)),
        ValueType::Instant => Value::Instant(j.as_i64().unwrap_or(0)),
        ValueType::Double | ValueType::Float32 | ValueType::Float16 | ValueType::Bfloat16 => {
            if let Some(s) = j.as_str() {
                match s {
                    "NaN" => Value::Double(f64::NAN),
                    "+inf" => Value::Double(f64::INFINITY),
                    "-inf" => Value::Double(f64::NEG_INFINITY),
                    _ => Value::Double(s.parse::<f64>().unwrap_or(0.0)),
                }
            } else { Value::Double(j.as_f64().unwrap_or(0.0)) }
        }
        ValueType::Boolean => Value::Boolean(j.as_bool().unwrap_or(false)),
        ValueType::String => Value::String(j.as_str().unwrap_or("").to_string()),
        ValueType::Keyword => Value::Keyword(j.as_str().unwrap_or("").to_string()),
        ValueType::Uuid => Value::Uuid(uuid::Uuid::parse_str(j.as_str().unwrap()).unwrap()),
        ValueType::Bytes => {
            if let Some(s) = j.as_str() { Value::Bytes(s.as_bytes().to_vec()) } else { Value::Bytes(vec![]) }
        }
        ValueType::Uint8 => Value::Uint8(j.as_u64().unwrap_or(0) as u8),
        ValueType::Bigint => Value::Bigint(j.as_str().unwrap_or("0").to_string()),
        ValueType::Decimal => Value::Decimal(j.as_str().unwrap_or("0").to_string()),
    }
}

pub fn normalize_grammar_ops(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    ops: &[J],
) -> Vec<TxOp> {
    let mut out = Vec::new();
    for op in ops {
        if let Some(list) = op.as_array() {
            if list.is_empty() { continue; }
            let tag = list[0].as_str().unwrap_or("");
            match tag {
                "add" => {
                    let e = parse_entity_ref(&list[1]);
                    let a = list[2].as_str().unwrap().to_string();
                    let attr = db.get_attr(&a).expect("unknown attribute in grammar");
                    let v = value_from_json_for_type(db, temps, alloc, attr.value_type, &list[3]);
                    out.push(TxOp::Add { e, a, v });
                }
                "retract" => {
                    let e = parse_entity_ref(&list[1]);
                    let a = list[2].as_str().unwrap().to_string();
                    let v = if list.len() > 3 { Some(parse_scalar_value(&list[3])) } else { None };
                    out.push(TxOp::Retract { e, a, v });
                }
                _ => {}
            }
        }
    }
    out
}

