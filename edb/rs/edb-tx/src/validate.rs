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
}

fn type_matches(v: &Value, vt: ValueType) -> bool {
    match (v, vt) {
        (Value::Long(_), ValueType::Long) => true,
        (Value::Double(_), ValueType::Double) => true,
        (Value::Double(_), ValueType::Float32) => true,
        (Value::Double(_), ValueType::Float16) => true,
        (Value::Double(_), ValueType::Bfloat16) => true,
        (Value::Boolean(_), ValueType::Boolean) => true,
        (Value::String(_), ValueType::String) => true,
        (Value::Keyword(_), ValueType::Keyword) => true,
        (Value::Uuid(_), ValueType::Uuid) => true,
        (Value::Instant(_), ValueType::Instant) => true,
        (Value::Ref(_), ValueType::Ref) => true,
        (Value::Bytes(_), ValueType::Bytes) => true,
        (Value::Uint8(_), ValueType::Uint8) => true,
        (Value::Bigint(_), ValueType::Bigint) => true,
        (Value::Decimal(_), ValueType::Decimal) => true,
        _ => false,
    }
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
                            // implicit retract
                            primitives.push(TxPrimitive { added: false, e: e_id, a: a.clone(), v: cur });
                        }
                    }
                }
                primitives.push(TxPrimitive { added: true, e: e_id, a: a.clone(), v: v.clone() });
                touched.insert(a.clone());
            }
            TxOp::Retract { e, a, v } => {
                let _attr = db.get_attr(a).ok_or_else(|| TxError::UnknownAttribute(a.clone()))?;
                let e_id = resolve_entity(db, e, &mut temps, alloc)?;
                if let Some(vv) = v.clone() {
                    primitives.push(TxPrimitive { added: false, e: e_id, a: a.clone(), v: vv });
                } else if let Some(cur) = db.current_value(e_id, a) {
                    primitives.push(TxPrimitive { added: false, e: e_id, a: a.clone(), v: cur });
                }
                touched.insert(a.clone());
            }
        }
    }

    let tempids = temps.map.into_iter().collect();
    Ok(TxReport { t: None, tx_eid: None, tempids, touched_attrs: touched.into_iter().collect(), primitives })
}
