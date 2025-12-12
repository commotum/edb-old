use std::collections::HashSet;

use edb_encoding::ValueType;
use edb_schema::{AttrCardinality, AttrUnique};

use crate::allocator::{EntidAllocator, TempResolver};
use crate::model::{EntityRef, TxOp, TxPrimitive, TxReport, Value};
use crate::traits::{DbView, UniquenessResult};

#[derive(thiserror::Error, Debug)]
pub enum TxError {
    #[error("unknown attribute: {0}")]
    UnknownAttribute(String),
    #[error("type mismatch for {attr}: expected {expected:?}")]
    TypeMismatch { attr: String, expected: ValueType },
    #[error("unique value conflict on {attr}")]
    UniqueValueConflict { attr: String },
    #[error("cas conflict on {attr}")]
    CasConflict { attr: String },
}

fn type_matches(v: &Value, vt: ValueType) -> bool {
    matches!(
        (v, vt),
        (Value::Long(_), ValueType::Long)
            | (Value::Double(_), ValueType::Double)
            | (Value::Double(_), ValueType::Float32)
            | (Value::Double(_), ValueType::Float16)
            | (Value::Double(_), ValueType::Bfloat16)
            | (Value::Boolean(_), ValueType::Boolean)
            | (Value::String(_), ValueType::String)
            | (Value::Keyword(_), ValueType::Keyword)
            | (Value::Uuid(_), ValueType::Uuid)
            | (Value::Instant(_), ValueType::Instant)
            | (Value::Ref(_), ValueType::Ref)
            | (Value::Bytes(_), ValueType::Bytes)
            | (Value::Uint8(_), ValueType::Uint8)
            | (Value::Bigint(_), ValueType::Bigint)
            | (Value::Decimal(_), ValueType::Decimal)
    )
}

fn resolve_entity(db: &dyn DbView, e: &EntityRef, temps: &mut TempResolver, alloc: &mut dyn EntidAllocator) -> Result<i64, TxError> {
    match e {
        EntityRef::Entid(id) => Ok(*id),
        EntityRef::TempId(t) => Ok(temps.resolve_or_alloc(t, alloc)),
        EntityRef::LookupRef { attr, value } => {
            if let Some(eid) = db.lookup_by_unique(attr, value) { Ok(eid) } else { Err(TxError::UnknownAttribute(attr.clone())) }
        }
    }
}

pub fn normalize_and_validate(
    db: &dyn DbView,
    ops: &[TxOp],
    alloc: &mut dyn EntidAllocator,
) -> Result<TxReport, TxError> {
    let mut temps = TempResolver::new();
    let mut primitives: Vec<TxPrimitive> = Vec::new();
    let mut touched: HashSet<String> = HashSet::new();

    // First pass: upsert tempids via unique identity attrs
    for op in ops.iter() {
        if let TxOp::Add { e, a, v } = op {
            if let Some(attr) = db.get_attr(a) {
                if matches!(attr.unique, AttrUnique::Identity) {
                    if let Some(existing) = db.lookup_by_unique(a, v) {
                        if let EntityRef::TempId(t) = e { temps.bind(t.clone(), existing); }
                    }
                }
            }
        }
    }

    // Second pass: resolve, validate, expand cardinality-one implicit retracts
    for op in ops.iter() {
        match op {
            TxOp::Add { e, a, v } => {
                let attr = db.get_attr(a).ok_or_else(|| TxError::UnknownAttribute(a.clone()))?;
                if !type_matches(v, attr.value_type) {
                    return Err(TxError::TypeMismatch { attr: a.clone(), expected: attr.value_type });
                }
                let e_id = resolve_entity(db, e, &mut temps, alloc)?;
                // uniqueness
                match attr.unique {
                    AttrUnique::Value => {
                        match db.uniqueness_check(a, v) {
                            UniquenessResult::Absent => {}
                            UniquenessResult::Present(owner) if owner == e_id => {}
                            _ => return Err(TxError::UniqueValueConflict { attr: a.clone() }),
                        }
                    }
                    AttrUnique::Identity => {
                        // already upserted by pre-pass; nothing more to do
                    }
                    AttrUnique::None => {}
                }
                if matches!(attr.cardinality, AttrCardinality::One) {
                    if let Some(cur) = db.current_value(e_id, a) {
                        if cur != *v {
                            // implicit retract (use canonical ident)
                            primitives.push(TxPrimitive { added: false, e: e_id, a: attr.ident.clone(), v: cur });
                        }
                    }
                }
                primitives.push(TxPrimitive { added: true, e: e_id, a: attr.ident.clone(), v: v.clone() });
                touched.insert(attr.ident.clone());
            }
            TxOp::Retract { e, a, v } => {
                let attr = db.get_attr(a).ok_or_else(|| TxError::UnknownAttribute(a.clone()))?;
                let e_id = resolve_entity(db, e, &mut temps, alloc)?;
                if let Some(vv) = v.clone() {
                    primitives.push(TxPrimitive { added: false, e: e_id, a: attr.ident.clone(), v: vv });
                } else if let Some(cur) = db.current_value(e_id, a) {
                    primitives.push(TxPrimitive { added: false, e: e_id, a: attr.ident.clone(), v: cur });
                }
                touched.insert(attr.ident.clone());
            }
            TxOp::Cas { e, a, expected, v } => {
                let attr = db.get_attr(a).ok_or_else(|| TxError::UnknownAttribute(a.clone()))?;
                if !type_matches(v, attr.value_type) {
                    return Err(TxError::TypeMismatch { attr: a.clone(), expected: attr.value_type });
                }
                let e_id = resolve_entity(db, e, &mut temps, alloc)?;
                // Check CAS expected vs current
                let cur = db.current_value(e_id, &attr.ident);
                if &cur != expected {
                    return Err(TxError::CasConflict { attr: a.clone() });
                }
                // uniqueness for new value
                match attr.unique {
                    AttrUnique::Value => {
                        match db.uniqueness_check(a, v) {
                            UniquenessResult::Absent => {}
                            UniquenessResult::Present(owner) if owner == e_id => {}
                            _ => return Err(TxError::UniqueValueConflict { attr: a.clone() }),
                        }
                    }
                    AttrUnique::Identity => {}
                    AttrUnique::None => {}
                }
                if matches!(attr.cardinality, AttrCardinality::One) {
                    if let Some(curv) = cur {
                        if curv != *v {
                            primitives.push(TxPrimitive { added: false, e: e_id, a: attr.ident.clone(), v: curv });
                        }
                    }
                }
                // Add new value if changed or if cardinality many (for now treat as replace for One)
                primitives.push(TxPrimitive { added: true, e: e_id, a: attr.ident.clone(), v: v.clone() });
                touched.insert(attr.ident.clone());
            }
        }
    }

    let tempids = temps.map.into_iter().collect();
    Ok(TxReport { t: None, tx_eid: None, tempids, touched_attrs: touched.into_iter().collect(), primitives, meta: None })
}
