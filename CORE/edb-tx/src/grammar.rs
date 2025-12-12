use serde_json::Value as J;

use crate::allocator::{EntidAllocator, TempResolver};
use crate::model::{EntityRef, TempId, TxOp, Value};
use crate::traits::DbView;
use edb_encoding::ValueType;
use crate::txfn::TxFnRegistry;

#[derive(Default)]
pub struct Normalized {
    pub ops: Vec<TxOp>,
    pub meta: Option<J>,
}

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
        ValueType::Long => {
            if let Some(n) = j.as_i64() { Value::Long(n) }
            else if let Some(s) = j.as_str() { if let Ok(n) = s.parse::<i64>() { Value::Long(n) } else { Value::String(s.to_string()) } }
            else { Value::String(j.to_string()) }
        }
        ValueType::Instant => {
            if let Some(n) = j.as_i64() { Value::Instant(n) }
            else if let Some(s) = j.as_str() { if let Ok(n) = s.parse::<i64>() { Value::Instant(n) } else { Value::String(s.to_string()) } }
            else { Value::String(j.to_string()) }
        }
        ValueType::Double | ValueType::Float32 | ValueType::Float16 | ValueType::Bfloat16 => {
            if let Some(s) = j.as_str() {
                match s {
                    "NaN" => Value::Double(f64::NAN),
                    "+inf" => Value::Double(f64::INFINITY),
                    "-inf" => Value::Double(f64::NEG_INFINITY),
                    _ => Value::Double(s.parse::<f64>().unwrap_or(0.0)),
                }
            } else if let Some(f) = j.as_f64() { Value::Double(f) } else { Value::String(j.to_string()) }
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

fn parse_map_form(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    map: &serde_json::Map<String, J>,
    out: &mut Vec<TxOp>,
) {
    let er = map.get("db/id").map(parse_entity_ref).unwrap_or_else(|| EntityRef::TempId(TempId("auto".into())));
    for (k, vj) in map.iter() {
        if k == "db/id" { continue; }
        if k.starts_with(':') {
            let a = k.clone();
            let attr = db.get_attr(&a).expect("unknown attribute in map form");
            let v = value_from_json_for_type(db, temps, alloc, attr.value_type, vj);
            out.push(TxOp::Add { e: er.clone(), a, v });
        }
    }
}

pub fn normalize_grammar(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    ops: &[J],
    txfns: Option<&TxFnRegistry>,
) -> Normalized {
    let mut out: Vec<TxOp> = Vec::new();
    let mut meta: Option<J> = None;
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
                "cas" => {
                    // ["cas", e, a, expected|null, new]
                    let er = parse_entity_ref(&list[1]);
                    let a = list[2].as_str().unwrap().to_string();
                    let attr = db.get_attr(&a).expect("unknown attribute in cas");
                    let expected = if list[3].is_null() { None } else { Some(value_from_json_for_type(db, temps, alloc, attr.value_type, &list[3])) };
                    let v = value_from_json_for_type(db, temps, alloc, attr.value_type, &list[4]);
                    out.push(TxOp::Cas { e: er, a, expected, v });
                }
                "tx-fn" => {
                    if let Some(reg) = txfns {
                        let fident = list[1].as_str().unwrap_or("").to_string();
                        if let Some(fun) = reg.get(&fident) {
                            let mut args: Vec<Value> = Vec::new();
                            for j in list.iter().skip(2) { args.push(parse_scalar_value(j)); }
                            if let Ok(mut ops2) = fun.apply(db, &args) { out.append(&mut ops2) }
                        }
                    }
                }
                "tx-meta" => {
                    if list.len() > 1 {
                        if let Some(m) = list[1].as_object() {
                            let mut merged = meta.take().unwrap_or(J::Object(serde_json::Map::new()));
                            let obj = merged.as_object_mut().unwrap();
                            for (k, v) in m.iter() { obj.insert(k.clone(), v.clone()); }
                            meta = Some(J::Object(obj.clone()));
                        }
                    }
                }
                "retract-entity" => {
                    // Expand retractEntity cascade using DbView
                    let er = parse_entity_ref(&list[1]);
                    // Resolve to entid
                    let eid = match er {
                        EntityRef::Entid(e) => e,
                        EntityRef::TempId(t) => temps.resolve_or_alloc(&t, alloc),
                        EntityRef::LookupRef { attr, value } => db.lookup_by_unique(&attr, &value).unwrap_or_else(|| temps.resolve_or_alloc(&TempId(format!("lkup:{}", attr)), alloc)),
                    };
                    fn cascade(db: &dyn DbView, eid: i64, out: &mut Vec<TxOp>) {
                        let attrs = db.entity_attrs(eid);
                        for (a, v) in attrs {
                            // Retract this attribute/value
                            out.push(TxOp::Retract { e: EntityRef::Entid(eid), a: a.clone(), v: Some(v.clone()) });
                            // Recurse if component ref
                            if let Some(attr) = db.get_attr(&a) {
                                if attr.is_component {
                                    if let Value::Ref(child) = v { cascade(db, child, out); }
                                }
                            }
                        }
                    }
                    cascade(db, eid, &mut out);
                }
                _ => {}
            }
        } else if let Some(map) = op.as_object() {
            parse_map_form(db, alloc, temps, map, &mut out);
        }
    }
    Normalized { ops: out, meta }
}
