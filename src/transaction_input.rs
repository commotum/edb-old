//! Transaction-only reference input and a bounded structural admission policy.
//! Stored Values and ordinary read identifiers are deliberately unchanged.
use crate::database::{UpsertIdentityValue, resolve_tuple_input};
use crate::{
    CallableRef, DatabaseValue, EntityRef, ErrorCategory, MapValue, RuntimeValue, SemanticError,
    TxForm, TxOp, TxValue, Value, ValueType,
};

// Like existing map/runtime depth policies, this bounds untrusted request
// structure, not the number of graph edges a query can traverse.
const MAX_REFERENCE_INPUT_DEPTH: usize = 32;
type LookupFn<'a> = dyn FnMut(u32, &Value) -> Result<Option<u64>, SemanticError> + 'a;

/// Match the existing stored-value decoding policy before a native caller's
/// owned values can enter recursive cloning, comparison, or encoding.
pub(crate) fn validate_stored_input(value: &Value) -> Result<(), SemanticError> {
    if !matches!(value, Value::Tuple(_)) {
        return Ok(());
    }
    let mut pending = vec![(value, 0usize)];
    while let Some((value, depth)) = pending.pop() {
        if depth > 16 {
            return Err(SemanticError::incorrect(
                "encoding/value-depth",
                "stored values exceed the 16-level admission policy",
            ));
        }
        if let Value::Tuple(slots) = value {
            pending.extend(slots.iter().flatten().map(|value| (value, depth + 1)));
        }
    }
    Ok(())
}

enum Input<'a> {
    Entity(&'a EntityRef, usize),
    Value(&'a TxValue, usize, bool),
}

fn validate(mut pending: Vec<Input<'_>>) -> Result<(), SemanticError> {
    while let Some(input) = pending.pop() {
        let depth = match input {
            Input::Entity(_, depth) | Input::Value(_, depth, _) => depth,
        };
        if depth > MAX_REFERENCE_INPUT_DEPTH {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "transaction/input-depth",
                "transaction reference input exceeds the 32-level admission policy",
            ));
        }
        match input {
            Input::Entity(EntityRef::Lookup { value, .. }, _)
            | Input::Value(TxValue::Scalar(value), _, _) => validate_stored_input(value)?,
            Input::Entity(EntityRef::LookupInput { value, .. }, depth) => {
                pending.push(Input::Value(value, depth + 1, false))
            }
            Input::Value(TxValue::Entity(entity), depth, _) => {
                pending.push(Input::Entity(entity, depth + 1))
            }
            Input::Value(TxValue::Tuple(slots), depth, nested) => {
                if nested || !(2..=8).contains(&slots.len()) {
                    return Err(SemanticError::incorrect(
                        "transaction/invalid-input-tuple",
                        "transaction tuples require 2–8 non-tuple slots",
                    ));
                }
                for slot in slots.iter().flatten() {
                    pending.push(Input::Value(slot, depth + 1, true));
                }
            }
            _ => {}
        }
    }
    Ok(())
}

pub(crate) fn validate_entity_input(entity: &EntityRef) -> Result<(), SemanticError> {
    validate(vec![Input::Entity(entity, 0)])
}
pub(crate) fn validate_value_input(value: &TxValue) -> Result<(), SemanticError> {
    validate(vec![Input::Value(value, 0, false)])
}
pub(crate) fn validate_ops_input(ops: &[TxOp]) -> Result<(), SemanticError> {
    for op in ops {
        match op {
            TxOp::Add { entity, value, .. } => {
                validate_entity_input(entity)?;
                validate_value_input(value)?;
            }
            TxOp::Retract { entity, value, .. } => {
                validate_entity_input(entity)?;
                if let Some(value) = value {
                    validate_value_input(value)?;
                }
            }
            TxOp::Cas {
                entity, old, new, ..
            } => {
                validate_entity_input(entity)?;
                if let Some(old) = old {
                    validate_value_input(old)?;
                }
                validate_value_input(new)?;
            }
            TxOp::RetractEntity(entity) => validate_entity_input(entity)?,
            TxOp::Ensure { entity, spec } => {
                validate_entity_input(entity)?;
                validate_entity_input(spec)?;
            }
            TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => {}
        }
    }
    Ok(())
}

pub(crate) fn validate_forms_input(forms: &[TxForm]) -> Result<(), SemanticError> {
    fn runtime(value: &RuntimeValue, depth: usize) -> Result<(), SemanticError> {
        if depth > 16 {
            return Err(SemanticError::incorrect(
                "program/value-depth",
                "runtime collection nesting exceeds the 16-level policy",
            ));
        }
        match value {
            RuntimeValue::Scalar(value) => validate_stored_input(value)?,
            RuntimeValue::Entity(entity) => validate_entity_input(entity)?,
            RuntimeValue::Vector(values) => {
                for value in values {
                    runtime(value, depth + 1)?;
                }
            }
            RuntimeValue::Map(entries) => {
                for (key, value) in entries {
                    validate_stored_input(key)?;
                    runtime(value, depth + 1)?;
                }
            }
            _ => {}
        }
        Ok(())
    }
    fn map(map: &crate::EntityMap, depth: usize) -> Result<(), SemanticError> {
        if let Some(id) = &map.id {
            validate_entity_input(id)?;
        }
        for (_, value) in &map.attributes {
            map_value(value, depth + 1)?;
        }
        Ok(())
    }
    fn map_value(value: &MapValue, depth: usize) -> Result<(), SemanticError> {
        if depth > 32 {
            return Err(SemanticError::incorrect(
                "transaction/map-depth",
                "transaction maps exceed the 32-level policy",
            ));
        }
        match value {
            MapValue::Value(value) => validate_value_input(value)?,
            MapValue::Nested(value) => map(value, depth)?,
            MapValue::Many(values) => {
                for value in values {
                    map_value(value, depth + 1)?;
                }
            }
        }
        Ok(())
    }
    for form in forms {
        match form {
            TxForm::Op(op) => validate_ops_input(std::slice::from_ref(op))?,
            TxForm::EntityMap(value) => map(value, 0)?,
            TxForm::Call(call) => {
                for value in &call.arguments {
                    validate_value_input(value)?;
                }
            }
            TxForm::ProgramCall(call) => {
                if let CallableRef::Database(entity) = &call.function {
                    validate_entity_input(entity)?;
                }
                for value in &call.arguments {
                    runtime(value, 0)?;
                }
            }
        }
    }
    Ok(())
}

impl DatabaseValue {
    /// Normalize a transaction-only reference-shaped lookup key against this
    /// exact db-before. Read context/cursor charging is retained throughout.
    pub(crate) fn resolve_lookup_input(
        &self,
        attribute: u32,
        input: &TxValue,
    ) -> Result<Option<u64>, SemanticError> {
        self.resolve_lookup_input_with(attribute, input, &mut |attribute, value| {
            self.lookup(attribute, value)
        })
    }

    pub(crate) fn resolve_lookup_input_with(
        &self,
        attribute: u32,
        input: &TxValue,
        lookup: &mut LookupFn<'_>,
    ) -> Result<Option<u64>, SemanticError> {
        validate_value_input(input)?;
        let descriptor = self.schema().attribute(attribute)?;
        if descriptor.unique.is_none() {
            return Err(SemanticError::incorrect(
                "transaction/lookup-non-unique",
                "lookup ref requires a unique attribute",
            ));
        }
        let value = self.lookup_input_value(attribute, input, lookup)?;
        lookup(attribute, &value)
    }

    fn lookup_input_value(
        &self,
        attribute: u32,
        input: &TxValue,
        lookup: &mut LookupFn<'_>,
    ) -> Result<Value, SemanticError> {
        let descriptor = self.schema().attribute(attribute)?;
        let value = match input {
            TxValue::Scalar(value) => {
                self.validate_lookup_stored_refs(value)?;
                value.clone()
            }
            TxValue::Entity(entity) if descriptor.value_type == ValueType::Ref => {
                Value::Ref(self.lookup_input_entity(entity, lookup)?)
            }
            TxValue::Tuple(slots) if descriptor.value_type == ValueType::Tuple => {
                for slot in slots.iter().flatten() {
                    if let TxValue::Scalar(value) = slot {
                        self.validate_lookup_stored_refs(value)?;
                    }
                }
                resolve_tuple_input(self.schema(), descriptor, slots, |entity| {
                    self.lookup_input_entity(entity, lookup)
                        .map(|entity| UpsertIdentityValue::Resolved(Value::Ref(entity)))
                })?
                .resolved()
                .expect("lookup input cannot retain tempids")
                .clone()
            }
            _ => {
                return Err(SemanticError::incorrect(
                    "transaction/value-type",
                    "lookup key does not match its attribute type",
                ));
            }
        };
        self.schema().validate_value(descriptor, &value)?;
        Ok(value)
    }

    fn lookup_input_entity(
        &self,
        entity: &EntityRef,
        lookup: &mut LookupFn<'_>,
    ) -> Result<u64, SemanticError> {
        let resolved = match entity {
            EntityRef::Id(entity) => {
                self.validate_lookup_stored_refs(&Value::Ref(*entity))?;
                Some(*entity)
            }
            EntityRef::Ident(ident) => self.entid(ident),
            EntityRef::Lookup { attribute, value } => lookup(*attribute, value)?,
            EntityRef::LookupInput { attribute, value } => {
                self.resolve_lookup_input_with(*attribute, value, lookup)?
            }
            EntityRef::Temp(_) | EntityRef::Tx => {
                return Err(SemanticError::incorrect(
                    "transaction/lookup-local-entity",
                    "lookup keys resolve only db-before entities, not tempids or the current transaction",
                ));
            }
        };
        resolved.ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/lookup-not-found",
                "lookup key reference did not resolve in db-before",
            )
        })
    }

    fn validate_lookup_stored_refs(&self, value: &Value) -> Result<(), SemanticError> {
        match value {
            Value::Ref(entity) => {
                crate::identity::validate_supported_eid(*entity)?;
                if crate::eid_to_eidx(*entity)? >= self.eidx_frontier() {
                    return Err(SemanticError::incorrect(
                        "transaction/invalid-entity-id",
                        "lookup key contains an unissued entity id",
                    ));
                }
            }
            Value::Tuple(slots) => {
                for value in slots.iter().flatten() {
                    self.validate_lookup_stored_refs(value)?;
                }
            }
            _ => {}
        }
        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn deep_value() -> Value {
        let mut value = Value::Long(1);
        for _ in 0..40 {
            value = Value::Tuple(vec![Some(value), None]);
        }
        value
    }

    #[test]
    fn native_stored_input_is_bounded_before_clone_sort_and_encoding() {
        let database = crate::Database::new(crate::Schema::new()).unwrap();
        let op = TxOp::Add {
            entity: EntityRef::Temp("e".into()),
            attribute: crate::DB_IDENT as u32,
            value: TxValue::Scalar(deep_value()),
        };
        assert_eq!(
            database
                .with(std::slice::from_ref(&op), 1)
                .unwrap_err()
                .code,
            "encoding/value-depth"
        );
        assert_eq!(
            database
                .database_value()
                .with(std::slice::from_ref(&op), 1)
                .unwrap_err()
                .code,
            "encoding/value-depth"
        );
        assert_eq!(
            crate::submission_request_digest(&[TxForm::Op(op)], None, None)
                .unwrap_err()
                .code,
            "encoding/value-depth"
        );
        for entity in [
            EntityRef::Lookup {
                attribute: crate::DB_IDENT as u32,
                value: deep_value(),
            },
            EntityRef::LookupInput {
                attribute: crate::DB_IDENT as u32,
                value: Box::new(TxValue::Scalar(deep_value())),
            },
        ] {
            assert_eq!(
                validate_entity_input(&entity).unwrap_err().code,
                "encoding/value-depth"
            );
        }
        let forms = [TxForm::ProgramCall(crate::ProgramCall {
            function: CallableRef::ExactHash([0; 32]),
            arguments: vec![RuntimeValue::Map(vec![(deep_value(), RuntimeValue::Null)])],
        })];
        assert_eq!(
            validate_forms_input(&forms).unwrap_err().code,
            "encoding/value-depth"
        );
        assert_eq!(
            RuntimeValue::map(vec![(deep_value(), RuntimeValue::Null)])
                .unwrap_err()
                .code,
            "encoding/value-depth"
        );
    }
}
