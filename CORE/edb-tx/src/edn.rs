use edb_edn::entities::{
    AttributePlace,
    Entity,
    EntityPlace,
    EntidOrIdent,
    LookupRef,
    OpType,
    TempId as EdnTempId,
    ValuePlace,
};
use edb_edn::{SpannedValue, ToMicros, ValueAndSpan};
use edb_encoding::ValueType;
use edb_schema::AttrCardinality;

use crate::allocator::{EntidAllocator, TempResolver};
use crate::grammar::Normalized;
use crate::model::{EntityRef, TempId, TxOp, Value};
use crate::traits::DbView;

#[derive(thiserror::Error, Debug)]
pub enum EdnTxError {
    #[error("edn parse error: {0}")]
    Parse(#[from] edb_edn::ParseError),
    #[error("unknown attribute: {0}")]
    UnknownAttribute(String),
    #[error("unsupported edn value: {0}")]
    Unsupported(String),
    #[error("invalid value: {0}")]
    InvalidValue(String),
}

pub fn normalize_edn(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    input: &str,
) -> Result<Normalized, EdnTxError> {
    let entities = edb_edn::parse::entities(input)?;
    let mut ops = Vec::new();
    for entity in entities {
        match entity {
            Entity::AddOrRetract { op, e, a, v } => {
                let e_ref = entity_ref_from_place(db, alloc, temps, &e)?;
                let a_ident = attr_ident_from_place(&a)?;
                let attr = db.get_attr(&a_ident).ok_or_else(|| EdnTxError::UnknownAttribute(a_ident.clone()))?;
                let values = values_from_place_for_attr(db, alloc, temps, &attr, &v)?;
                match op {
                    OpType::Add => {
                        for value in values {
                            ops.push(TxOp::Add { e: e_ref.clone(), a: a_ident.clone(), v: value });
                        }
                    }
                    OpType::Retract => {
                        for value in values {
                            ops.push(TxOp::Retract { e: e_ref.clone(), a: a_ident.clone(), v: Some(value) });
                        }
                    }
                }
            }
            Entity::MapNotation(map) => {
                let mut e_ref = EntityRef::TempId(TempId("auto".to_string()));
                for (k, v) in map.iter() {
                    match k {
                        EntidOrIdent::Ident(kw) if kw.to_string() == ":db/id" => {
                            e_ref = entity_ref_from_value_place(db, alloc, temps, v)?;
                        }
                        _ => {}
                    }
                }
                for (k, v) in map.iter() {
                    let a_ident = match k {
                        EntidOrIdent::Ident(kw) => kw.to_string(),
                        EntidOrIdent::Entid(id) => {
                            return Err(EdnTxError::Unsupported(format!("attribute entid {id} not supported")));
                        }
                    };
                    if a_ident == ":db/id" {
                        continue;
                    }
                    let attr = db.get_attr(&a_ident).ok_or_else(|| EdnTxError::UnknownAttribute(a_ident.clone()))?;
                    let values = values_from_place_for_attr(db, alloc, temps, &attr, v)?;
                    for value in values {
                        ops.push(TxOp::Add { e: e_ref.clone(), a: a_ident.clone(), v: value });
                    }
                }
            }
        }
    }
    Ok(Normalized { ops, meta: None })
}

fn tempid_from_edn(tempid: &EdnTempId) -> TempId {
    match tempid {
        EdnTempId::External(s) => TempId(s.clone()),
        EdnTempId::Internal(id) => TempId(format!("internal:{id}")),
    }
}

fn attr_ident_from_place(attr: &AttributePlace) -> Result<String, EdnTxError> {
    match attr {
        AttributePlace::Entid(EntidOrIdent::Ident(kw)) => Ok(kw.to_string()),
        AttributePlace::Entid(EntidOrIdent::Entid(id)) => {
            Err(EdnTxError::Unsupported(format!("attribute entid {id} not supported")))
        }
    }
}

fn entity_ref_from_place(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    place: &EntityPlace<ValueAndSpan>,
) -> Result<EntityRef, EdnTxError> {
    match place {
        EntityPlace::Entid(EntidOrIdent::Entid(id)) => Ok(EntityRef::Entid(*id)),
        EntityPlace::Entid(EntidOrIdent::Ident(kw)) => Ok(EntityRef::TempId(TempId(kw.to_string()))),
        EntityPlace::TempId(temp) => Ok(EntityRef::TempId(tempid_from_edn(temp.as_ref()))),
        EntityPlace::LookupRef(lookup) => lookup_ref_to_entity_ref(db, alloc, temps, lookup),
        EntityPlace::TxFunction(fun) => Err(EdnTxError::Unsupported(format!("tx function {} not supported", fun.op))),
    }
}

fn entity_ref_from_value_place(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    place: &ValuePlace<ValueAndSpan>,
) -> Result<EntityRef, EdnTxError> {
    match place {
        ValuePlace::Atom(v) => entity_ref_from_atom(v),
        ValuePlace::Entid(entid) => entity_ref_from_entid(entid),
        ValuePlace::TempId(temp) => Ok(EntityRef::TempId(tempid_from_edn(temp.as_ref()))),
        ValuePlace::LookupRef(lookup) => lookup_ref_to_entity_ref(db, alloc, temps, lookup),
        ValuePlace::TxFunction(fun) => Err(EdnTxError::Unsupported(format!("tx function {} not supported", fun.op))),
        ValuePlace::Vector(_) => Err(EdnTxError::Unsupported("vector not allowed for :db/id".to_string())),
        ValuePlace::MapNotation(_) => Err(EdnTxError::Unsupported("map notation not allowed for :db/id".to_string())),
    }
}

fn entity_ref_from_atom(value: &ValueAndSpan) -> Result<EntityRef, EdnTxError> {
    match value.inner {
        SpannedValue::Integer(id) => Ok(EntityRef::Entid(id)),
        SpannedValue::BigInteger(ref id) => id
            .to_string()
            .parse::<i64>()
            .map(EntityRef::Entid)
            .map_err(|_| EdnTxError::InvalidValue("invalid entid bigint".to_string())),
        SpannedValue::Text(ref s) => Ok(EntityRef::TempId(TempId(s.clone()))),
        SpannedValue::Keyword(ref kw) => Ok(EntityRef::TempId(TempId(kw.to_string()))),
        _ => Err(EdnTxError::InvalidValue("invalid :db/id value".to_string())),
    }
}

fn entity_ref_from_entid(entid: &EntidOrIdent) -> Result<EntityRef, EdnTxError> {
    match entid {
        EntidOrIdent::Entid(id) => Ok(EntityRef::Entid(*id)),
        EntidOrIdent::Ident(kw) => Ok(EntityRef::TempId(TempId(kw.to_string()))),
    }
}

fn lookup_ref_to_entity_ref(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    lookup: &LookupRef<ValueAndSpan>,
) -> Result<EntityRef, EdnTxError> {
    let attr = attr_ident_from_place(&lookup.a)?;
    let attr_meta = db.get_attr(&attr).ok_or_else(|| EdnTxError::UnknownAttribute(attr.clone()))?;
    let value = value_from_atom_for_type(db, alloc, temps, attr_meta.value_type, &lookup.v)?;
    Ok(EntityRef::LookupRef { attr, value })
}

fn values_from_place_for_attr(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    attr: &edb_schema::Attribute,
    place: &ValuePlace<ValueAndSpan>,
) -> Result<Vec<Value>, EdnTxError> {
    match place {
        ValuePlace::Vector(values) => {
            if !matches!(attr.cardinality, AttrCardinality::Many) {
                return Err(EdnTxError::InvalidValue(format!(
                    "attribute {} is cardinality-one but received a vector",
                    attr.ident
                )));
            }
            let mut out = Vec::new();
            for value in values {
                out.push(value_from_place_for_type(db, alloc, temps, attr.value_type, value)?);
            }
            Ok(out)
        }
        ValuePlace::MapNotation(_) => Err(EdnTxError::Unsupported("map notation values are not supported".to_string())),
        _ => Ok(vec![value_from_place_for_type(db, alloc, temps, attr.value_type, place)?]),
    }
}

fn value_from_place_for_type(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    vt: ValueType,
    place: &ValuePlace<ValueAndSpan>,
) -> Result<Value, EdnTxError> {
    match vt {
        ValueType::Ref => value_from_ref_place(db, alloc, temps, place),
        _ => match place {
            ValuePlace::Atom(v) => value_from_atom_for_type(db, alloc, temps, vt, v),
            _ => Err(EdnTxError::Unsupported("non-atom value for scalar attribute".to_string())),
        },
    }
}

fn value_from_ref_place(
    db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    place: &ValuePlace<ValueAndSpan>,
) -> Result<Value, EdnTxError> {
    match place {
        ValuePlace::Atom(v) => value_ref_from_atom(alloc, temps, v),
        ValuePlace::Entid(entid) => match entid {
            EntidOrIdent::Entid(id) => Ok(Value::Ref(*id)),
            EntidOrIdent::Ident(kw) => {
                let temp = TempId(kw.to_string());
                Ok(Value::Ref(temps.resolve_or_alloc(&temp, alloc)))
            }
        },
        ValuePlace::TempId(temp) => {
            let tempid = tempid_from_edn(temp.as_ref());
            Ok(Value::Ref(temps.resolve_or_alloc(&tempid, alloc)))
        }
        ValuePlace::LookupRef(lookup) => {
            let attr = attr_ident_from_place(&lookup.a)?;
            let attr_meta = db.get_attr(&attr).ok_or_else(|| EdnTxError::UnknownAttribute(attr.clone()))?;
            let lookup_value = value_from_atom_for_type(db, alloc, temps, attr_meta.value_type, &lookup.v)?;
            let eid = db
                .lookup_by_unique(&attr, &lookup_value)
                .unwrap_or_else(|| temps.resolve_or_alloc(&TempId(format!("lkup:{attr}")), alloc));
            Ok(Value::Ref(eid))
        }
        ValuePlace::TxFunction(fun) => Err(EdnTxError::Unsupported(format!("tx function {} not supported", fun.op))),
        ValuePlace::Vector(_) => Err(EdnTxError::Unsupported("vector not allowed for ref value".to_string())),
        ValuePlace::MapNotation(_) => Err(EdnTxError::Unsupported("map notation not allowed for ref value".to_string())),
    }
}

fn value_ref_from_atom(
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    value: &ValueAndSpan,
) -> Result<Value, EdnTxError> {
    match value.inner {
        SpannedValue::Integer(id) => Ok(Value::Ref(id)),
        SpannedValue::BigInteger(ref id) => id
            .to_string()
            .parse::<i64>()
            .map(Value::Ref)
            .map_err(|_| EdnTxError::InvalidValue("invalid ref bigint".to_string())),
        SpannedValue::Text(ref s) => {
            let temp = TempId(s.clone());
            Ok(Value::Ref(temps.resolve_or_alloc(&temp, alloc)))
        }
        SpannedValue::Keyword(ref kw) => {
            let temp = TempId(kw.to_string());
            Ok(Value::Ref(temps.resolve_or_alloc(&temp, alloc)))
        }
        _ => Err(EdnTxError::InvalidValue("invalid ref value".to_string())),
    }
}

fn value_from_atom_for_type(
    _db: &dyn DbView,
    alloc: &mut dyn EntidAllocator,
    temps: &mut TempResolver,
    vt: ValueType,
    value: &ValueAndSpan,
) -> Result<Value, EdnTxError> {
    match vt {
        ValueType::Long => match value.inner {
            SpannedValue::Integer(v) => Ok(Value::Long(v)),
            SpannedValue::BigInteger(ref v) => v
                .to_string()
                .parse::<i64>()
                .map(Value::Long)
                .map_err(|_| EdnTxError::InvalidValue("invalid bigint for long".to_string())),
            SpannedValue::Text(ref s) => s
                .parse::<i64>()
                .map(Value::Long)
                .map_err(|_| EdnTxError::InvalidValue("invalid string for long".to_string())),
            _ => Err(EdnTxError::InvalidValue("expected long".to_string())),
        },
        ValueType::Instant => match value.inner {
            SpannedValue::Instant(v) => Ok(Value::Instant(v.to_micros())),
            SpannedValue::Integer(v) => Ok(Value::Instant(v)),
            SpannedValue::Text(ref s) => s
                .parse::<i64>()
                .map(Value::Instant)
                .map_err(|_| EdnTxError::InvalidValue("invalid string for instant".to_string())),
            _ => Err(EdnTxError::InvalidValue("expected instant".to_string())),
        },
        ValueType::Double | ValueType::Float32 | ValueType::Float16 | ValueType::Bfloat16 => match value.inner {
            SpannedValue::Float(v) => Ok(Value::Double(v.into_inner())),
            SpannedValue::Integer(v) => Ok(Value::Double(v as f64)),
            SpannedValue::BigInteger(ref v) => v
                .to_string()
                .parse::<f64>()
                .map(Value::Double)
                .map_err(|_| EdnTxError::InvalidValue("invalid bigint for double".to_string())),
            SpannedValue::Text(ref s) => s
                .parse::<f64>()
                .map(Value::Double)
                .map_err(|_| EdnTxError::InvalidValue("invalid string for double".to_string())),
            _ => Err(EdnTxError::InvalidValue("expected double".to_string())),
        },
        ValueType::Boolean => match value.inner {
            SpannedValue::Boolean(v) => Ok(Value::Boolean(v)),
            _ => Err(EdnTxError::InvalidValue("expected boolean".to_string())),
        },
        ValueType::String => match value.inner {
            SpannedValue::Text(ref s) => Ok(Value::String(s.clone())),
            SpannedValue::Keyword(ref kw) => Ok(Value::String(kw.to_string())),
            SpannedValue::PlainSymbol(ref sym) => Ok(Value::String(sym.to_string())),
            _ => Ok(Value::String(value.to_string())),
        },
        ValueType::Keyword => match value.inner {
            SpannedValue::Keyword(ref kw) => Ok(Value::Keyword(kw.to_string())),
            SpannedValue::Text(ref s) => Ok(Value::Keyword(s.clone())),
            _ => Err(EdnTxError::InvalidValue("expected keyword".to_string())),
        },
        ValueType::Uuid => match value.inner {
            SpannedValue::Uuid(u) => Ok(Value::Uuid(u)),
            SpannedValue::Text(ref s) => uuid::Uuid::parse_str(s)
                .map(Value::Uuid)
                .map_err(|_| EdnTxError::InvalidValue("invalid uuid".to_string())),
            _ => Err(EdnTxError::InvalidValue("expected uuid".to_string())),
        },
        ValueType::Bytes => match value.inner {
            SpannedValue::Text(ref s) => Ok(Value::Bytes(s.as_bytes().to_vec())),
            _ => Err(EdnTxError::InvalidValue("expected bytes".to_string())),
        },
        ValueType::Uint8 => match value.inner {
            SpannedValue::Integer(v) => Ok(Value::Uint8(v as u8)),
            SpannedValue::Text(ref s) => s
                .parse::<u8>()
                .map(Value::Uint8)
                .map_err(|_| EdnTxError::InvalidValue("invalid uint8".to_string())),
            _ => Err(EdnTxError::InvalidValue("expected uint8".to_string())),
        },
        ValueType::Bigint => match value.inner {
            SpannedValue::BigInteger(ref v) => Ok(Value::Bigint(v.to_string())),
            SpannedValue::Integer(v) => Ok(Value::Bigint(v.to_string())),
            SpannedValue::Text(ref s) => Ok(Value::Bigint(s.clone())),
            _ => Err(EdnTxError::InvalidValue("expected bigint".to_string())),
        },
        ValueType::Decimal => match value.inner {
            SpannedValue::Decimal(ref v) => Ok(Value::Decimal(v.to_string())),
            SpannedValue::Integer(v) => Ok(Value::Decimal(v.to_string())),
            SpannedValue::Float(v) => Ok(Value::Decimal(v.to_string())),
            SpannedValue::Text(ref s) => Ok(Value::Decimal(s.clone())),
            _ => Err(EdnTxError::InvalidValue("expected decimal".to_string())),
        },
        ValueType::Ref => value_ref_from_atom(alloc, temps, value),
    }
}
