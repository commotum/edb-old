use crate::{Keyword, SemanticError, Value};
use std::collections::BTreeMap;

pub type AttrId = u32;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Cardinality {
    One,
    Many,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Unique {
    Identity,
    Value,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ValueType {
    BigDec,
    BigInt,
    Boolean,
    Bytes,
    Double,
    Float,
    Instant,
    Keyword,
    Long,
    Ref,
    String,
    Symbol,
    Tuple,
    Uuid,
    Uri,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum TupleSpec {
    Homogeneous(ValueType),
    Heterogeneous(Vec<ValueType>),
    Composite(Vec<AttrId>),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Attribute {
    pub id: AttrId,
    pub ident: Keyword,
    pub value_type: ValueType,
    pub cardinality: Cardinality,
    pub unique: Option<Unique>,
    pub indexed: bool,
    pub component: bool,
    pub no_history: bool,
    pub tuple: Option<TupleSpec>,
    pub tuple_discontinued: bool,
    /// Names of deterministic value predicates. Implementations are supplied
    /// by the process-local kernel context; persisted deployment is later.
    pub predicates: Vec<String>,
}

impl Attribute {
    pub fn new(
        id: AttrId,
        ident: Keyword,
        value_type: ValueType,
        cardinality: Cardinality,
    ) -> Self {
        Self {
            id,
            ident,
            value_type,
            cardinality,
            unique: None,
            indexed: false,
            component: false,
            no_history: false,
            tuple: None,
            tuple_discontinued: false,
            predicates: Vec::new(),
        }
    }

    pub fn unique(mut self, unique: Unique) -> Self {
        self.unique = Some(unique);
        self.indexed = true;
        self
    }

    pub fn component(mut self) -> Self {
        self.component = true;
        self
    }

    pub fn tuple(mut self, tuple: TupleSpec) -> Self {
        self.tuple = Some(tuple);
        self
    }

    pub fn predicate(mut self, name: impl Into<String>) -> Self {
        self.predicates.push(name.into());
        self.predicates.sort();
        self.predicates.dedup();
        self
    }
}

#[derive(Clone, Debug, Default)]
pub struct Schema {
    attributes: BTreeMap<AttrId, Attribute>,
    current_idents: BTreeMap<Keyword, AttrId>,
    ident_aliases: BTreeMap<Keyword, AttrId>,
}

impl Schema {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn install(&mut self, attribute: Attribute) -> Result<(), SemanticError> {
        self.validate_attribute(&attribute)?;
        if self.attributes.contains_key(&attribute.id) {
            return Err(SemanticError::conflict(
                "schema/attribute-id-exists",
                format!("attribute id {} is already installed", attribute.id),
            ));
        }
        if let Some(existing) = self.current_idents.get(&attribute.ident) {
            return Err(SemanticError::conflict(
                "schema/ident-exists",
                format!(
                    "ident {} already names attribute {existing}",
                    attribute.ident.qualified_name()
                ),
            ));
        }
        self.current_idents
            .insert(attribute.ident.clone(), attribute.id);
        self.attributes.insert(attribute.id, attribute);
        Ok(())
    }

    /// Renaming preserves the old ident as an alias, matching documented
    /// entity resolution while making the new ident canonical.
    pub fn rename(&mut self, id: AttrId, ident: Keyword) -> Result<(), SemanticError> {
        if let Some(existing) = self.current_idents.get(&ident)
            && *existing != id
        {
            return Err(SemanticError::conflict(
                "schema/ident-exists",
                format!("ident {} is already in use", ident.qualified_name()),
            ));
        }
        let attribute = self.attributes.get_mut(&id).ok_or_else(|| {
            SemanticError::incorrect(
                "schema/unknown-attribute",
                format!("unknown attribute id {id}"),
            )
        })?;
        let old = std::mem::replace(&mut attribute.ident, ident.clone());
        self.current_idents.remove(&old);
        self.ident_aliases.insert(old, id);
        self.current_idents.insert(ident, id);
        Ok(())
    }

    /// Replace the mutable properties of an installed attribute.
    ///
    /// Database-dependent transition checks (cardinality and uniqueness over
    /// current facts) are performed by the transaction kernel before this is
    /// called. Keeping this operation on the schema cache mirrors recovered
    /// `alter-attribute`, which updates the immutable database value only after
    /// the complete transaction has passed its hooks.
    pub(crate) fn alter(&mut self, proposed: Attribute) -> Result<(), SemanticError> {
        self.validate_attribute(&proposed)?;
        let current = self.attributes.get(&proposed.id).ok_or_else(|| {
            SemanticError::incorrect(
                "schema/unknown-attribute",
                format!("unknown attribute id {}", proposed.id),
            )
        })?;
        if current.value_type != proposed.value_type {
            return Err(SemanticError::incorrect(
                "schema/value-type-immutable",
                "an installed attribute's value type cannot change",
            ));
        }
        if current.tuple != proposed.tuple {
            return Err(SemanticError::incorrect(
                "schema/tuple-definition-immutable",
                "an installed tuple definition cannot change",
            ));
        }
        if current.tuple_discontinued && !proposed.tuple_discontinued {
            return Err(SemanticError::incorrect(
                "schema/tuple-discontinuation-irreversible",
                "a discontinued composite tuple cannot be resumed",
            ));
        }

        if current.ident != proposed.ident {
            if let Some(existing) = self.current_idents.get(&proposed.ident)
                && *existing != proposed.id
            {
                return Err(SemanticError::conflict(
                    "schema/ident-exists",
                    format!(
                        "ident {} is already in use",
                        proposed.ident.qualified_name()
                    ),
                ));
            }
            let old_ident = current.ident.clone();
            self.current_idents.remove(&old_ident);
            self.ident_aliases.insert(old_ident, proposed.id);
            self.current_idents
                .insert(proposed.ident.clone(), proposed.id);
        }
        self.attributes.insert(proposed.id, proposed);
        Ok(())
    }

    pub fn attribute(&self, id: AttrId) -> Result<&Attribute, SemanticError> {
        self.attributes.get(&id).ok_or_else(|| {
            SemanticError::incorrect(
                "schema/unknown-attribute",
                format!("unknown attribute id {id}"),
            )
        })
    }

    pub fn resolve_ident(&self, ident: &Keyword) -> Option<AttrId> {
        self.current_idents
            .get(ident)
            .or_else(|| self.ident_aliases.get(ident))
            .copied()
    }

    pub fn attributes(&self) -> impl Iterator<Item = &Attribute> {
        self.attributes.values()
    }

    pub(crate) fn ident_aliases(&self) -> impl Iterator<Item = (&Keyword, AttrId)> {
        self.ident_aliases
            .iter()
            .map(|(ident, attribute)| (ident, *attribute))
    }

    pub(crate) fn install_ident_alias(
        &mut self,
        ident: Keyword,
        attribute: AttrId,
    ) -> Result<(), SemanticError> {
        if !self.attributes.contains_key(&attribute) {
            return Err(SemanticError::incorrect(
                "schema/unknown-attribute",
                format!("alias target attribute {attribute} is not installed"),
            ));
        }
        if self.current_idents.contains_key(&ident) || self.ident_aliases.contains_key(&ident) {
            return Err(SemanticError::conflict(
                "schema/ident-exists",
                format!("ident {} is already in use", ident.qualified_name()),
            ));
        }
        self.ident_aliases.insert(ident, attribute);
        Ok(())
    }

    pub fn validate_value(
        &self,
        attribute: &Attribute,
        value: &Value,
    ) -> Result<(), SemanticError> {
        if !matches_value_type(attribute.value_type, value) {
            return Err(SemanticError::incorrect(
                "transaction/value-type",
                format!(
                    "attribute {} requires {:?}, got {}",
                    attribute.ident.qualified_name(),
                    attribute.value_type,
                    value.type_name()
                ),
            ));
        }

        match value {
            Value::BigInt(value) if value.bits() > 8192 => Err(SemanticError::incorrect(
                "transaction/bigint-too-large",
                "BigInteger values are limited to 8192 bits",
            )),
            Value::BigDec(value) if value.digits() > 1024 => Err(SemanticError::incorrect(
                "transaction/bigdec-too-large",
                "BigDecimal values are limited to 1024 digits",
            )),
            Value::Tuple(slots) => self.validate_tuple(attribute, slots),
            _ => Ok(()),
        }
    }

    pub(crate) fn validate_attribute(&self, attribute: &Attribute) -> Result<(), SemanticError> {
        if attribute.unique.is_some() && attribute.cardinality != Cardinality::One {
            return Err(SemanticError::incorrect(
                "schema/unique-must-be-cardinality-one",
                "unique attributes must have cardinality one",
            ));
        }
        if attribute.unique.is_some() && attribute.value_type == ValueType::Bytes {
            return Err(SemanticError::incorrect(
                "schema/bytes-cannot-be-unique",
                "bytes attributes cannot be unique",
            ));
        }
        if attribute.component && attribute.value_type != ValueType::Ref {
            return Err(SemanticError::incorrect(
                "schema/component-must-be-ref",
                "component attributes must have ref value type",
            ));
        }
        match (&attribute.value_type, &attribute.tuple) {
            (ValueType::Tuple, Some(TupleSpec::Heterogeneous(types)))
                if !(2..=8).contains(&types.len()) =>
            {
                Err(SemanticError::incorrect(
                    "schema/invalid-tuple-length",
                    "heterogeneous tuples require 2 through 8 slots",
                ))
            }
            (ValueType::Tuple, Some(TupleSpec::Composite(attributes)))
                if !(2..=8).contains(&attributes.len()) =>
            {
                Err(SemanticError::incorrect(
                    "schema/invalid-tuple-length",
                    "composite tuples require 2 through 8 attributes",
                ))
            }
            (ValueType::Tuple, Some(_)) => Ok(()),
            (ValueType::Tuple, None) => Err(SemanticError::incorrect(
                "schema/missing-tuple-spec",
                "tuple attributes require one tuple specification",
            )),
            (_, Some(_)) => Err(SemanticError::incorrect(
                "schema/tuple-spec-on-scalar",
                "tuple specifications are valid only for tuple attributes",
            )),
            (_, None) => Ok(()),
        }
    }

    fn validate_tuple(
        &self,
        attribute: &Attribute,
        slots: &[Option<Value>],
    ) -> Result<(), SemanticError> {
        let tuple = attribute.tuple.as_ref().ok_or_else(|| {
            SemanticError::incorrect("schema/missing-tuple-spec", "tuple specification missing")
        })?;
        match tuple {
            TupleSpec::Homogeneous(value_type) => {
                if !(2..=8).contains(&slots.len()) {
                    return Err(SemanticError::incorrect(
                        "transaction/invalid-tuple-length",
                        "homogeneous tuples require 2 through 8 slots",
                    ));
                }
                for slot in slots.iter().flatten() {
                    validate_tuple_slot(*value_type, slot)?;
                }
            }
            TupleSpec::Heterogeneous(types) => {
                if slots.len() != types.len() {
                    return Err(SemanticError::incorrect(
                        "transaction/invalid-tuple-length",
                        format!("tuple requires {} slots", types.len()),
                    ));
                }
                for (value_type, slot) in types
                    .iter()
                    .zip(slots)
                    .filter_map(|(value_type, slot)| slot.as_ref().map(|slot| (value_type, slot)))
                {
                    validate_tuple_slot(*value_type, slot)?;
                }
            }
            TupleSpec::Composite(attributes) => {
                if slots.len() != attributes.len() {
                    return Err(SemanticError::incorrect(
                        "transaction/invalid-tuple-length",
                        format!("composite tuple requires {} slots", attributes.len()),
                    ));
                }
                for (attribute_id, slot) in attributes.iter().zip(slots) {
                    if let Some(slot) = slot {
                        let constituent = self.attribute(*attribute_id)?;
                        validate_tuple_slot(constituent.value_type, slot)?;
                    }
                }
            }
        }
        Ok(())
    }
}

fn matches_value_type(value_type: ValueType, value: &Value) -> bool {
    matches!(
        (value_type, value),
        (ValueType::BigDec, Value::BigDec(_))
            | (ValueType::BigInt, Value::BigInt(_))
            | (ValueType::Boolean, Value::Bool(_))
            | (ValueType::Bytes, Value::Bytes(_))
            | (ValueType::Double, Value::Double(_))
            | (ValueType::Float, Value::Float(_))
            | (ValueType::Instant, Value::Instant(_))
            | (ValueType::Keyword, Value::Keyword(_))
            | (ValueType::Long, Value::Long(_))
            | (ValueType::Ref, Value::Ref(_))
            | (ValueType::String, Value::String(_))
            | (ValueType::Symbol, Value::Symbol(_))
            | (ValueType::Tuple, Value::Tuple(_))
            | (ValueType::Uuid, Value::Uuid(_))
            | (ValueType::Uri, Value::Uri(_))
    )
}

fn validate_tuple_slot(value_type: ValueType, value: &Value) -> Result<(), SemanticError> {
    if value_type == ValueType::Tuple || value_type == ValueType::Bytes {
        return Err(SemanticError::incorrect(
            "schema/invalid-tuple-element-type",
            "tuples and bytes cannot be tuple elements",
        ));
    }
    if !matches_value_type(value_type, value) {
        return Err(SemanticError::incorrect(
            "transaction/invalid-tuple-element",
            format!(
                "tuple slot requires {value_type:?}, got {}",
                value.type_name()
            ),
        ));
    }
    match value {
        Value::String(value) if value.chars().count() > 256 => Err(SemanticError::incorrect(
            "transaction/tuple-string-too-large",
            "tuple strings are limited to 256 characters",
        )),
        Value::BigInt(value) if value.bits() > 256 => Err(SemanticError::incorrect(
            "transaction/tuple-bigint-too-large",
            "tuple BigIntegers are limited to 256 bits",
        )),
        Value::BigDec(value) if value.digits() > 256 => Err(SemanticError::incorrect(
            "transaction/tuple-bigdec-too-large",
            "tuple BigDecimals are limited to 256 digits",
        )),
        _ => Ok(()),
    }
}
