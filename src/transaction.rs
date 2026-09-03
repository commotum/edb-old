use crate::{
    Cardinality, Database, EntityRef, Keyword, SemanticError, TxOp, TxReport, TxValue, Unique,
    ValueType,
};
use std::collections::BTreeMap;
use std::fmt;
use std::sync::Arc;

#[derive(Clone, Debug)]
pub enum AttributeRef {
    Id(u32),
    Ident(Keyword),
    ReverseId(u32),
    ReverseIdent(Keyword),
}

#[derive(Clone, Debug)]
pub enum MapValue {
    Value(TxValue),
    Nested(Box<EntityMap>),
    Many(Vec<MapValue>),
}

impl From<TxValue> for MapValue {
    fn from(value: TxValue) -> Self {
        Self::Value(value)
    }
}

#[derive(Clone, Debug)]
pub struct EntityMap {
    pub id: Option<EntityRef>,
    pub attributes: Vec<(AttributeRef, MapValue)>,
}

#[derive(Clone, Debug)]
pub struct TxCall {
    pub function: String,
    pub arguments: Vec<TxValue>,
}

#[derive(Clone, Debug)]
pub enum TxForm {
    Op(TxOp),
    EntityMap(EntityMap),
    Call(TxCall),
}

type NativeFunction =
    dyn Fn(&Database, &[TxValue]) -> Result<Vec<TxForm>, SemanticError> + Send + Sync;
type NativePredicate = dyn Fn(&crate::Value) -> Result<bool, SemanticError> + Send + Sync;

/// Process-local deterministic transaction functions.
///
/// Persisted/sandboxed functions are deliberately not represented here. This
/// boundary proves the documented `[db-before, args] -> tx-data` semantics
/// without importing JVM classpath behavior into the native kernel.
#[derive(Clone, Default)]
pub struct TxFunctions {
    functions: BTreeMap<String, Arc<NativeFunction>>,
    predicates: BTreeMap<String, Arc<NativePredicate>>,
}

impl fmt::Debug for TxFunctions {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("TxFunctions")
            .field("names", &self.functions.keys().collect::<Vec<_>>())
            .field("predicates", &self.predicates.keys().collect::<Vec<_>>())
            .finish()
    }
}

impl TxFunctions {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn register<F>(&mut self, name: impl Into<String>, function: F)
    where
        F: Fn(&Database, &[TxValue]) -> Result<Vec<TxForm>, SemanticError> + Send + Sync + 'static,
    {
        self.functions.insert(name.into(), Arc::new(function));
    }

    pub fn register_attribute_predicate<F>(&mut self, name: impl Into<String>, predicate: F)
    where
        F: Fn(&crate::Value) -> Result<bool, SemanticError> + Send + Sync + 'static,
    {
        self.predicates.insert(name.into(), Arc::new(predicate));
    }

    pub(crate) fn validate_attribute_predicate(
        &self,
        name: &str,
        value: &crate::Value,
    ) -> Result<bool, SemanticError> {
        self.predicates.get(name).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-attribute-predicate",
                format!("unknown attribute predicate {name}"),
            )
        })?(value)
    }

    fn invoke(&self, db_before: &Database, call: &TxCall) -> Result<Vec<TxForm>, SemanticError> {
        let function = self.functions.get(&call.function).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-function",
                format!("unknown transaction function {}", call.function),
            )
        })?;
        function(db_before, &call.arguments)
    }
}

impl Database {
    /// Normalize map and function forms against one db-before, then apply the
    /// resulting complete primitive information set through `with`.
    pub fn with_forms(
        &self,
        forms: &[TxForm],
        functions: &TxFunctions,
        tx_instant: i64,
    ) -> Result<TxReport, SemanticError> {
        let mut normalizer = Normalizer {
            db_before: self,
            functions,
            next_anonymous: 0,
            primitive_count: 0,
        };
        let mut forms = forms.to_vec();
        forms.sort_by_key(|form| format!("{form:?}"));
        let mut ops = Vec::new();
        for form in &forms {
            normalizer.expand_form(form, 0, &mut ops)?;
        }
        self.with_function_context(&ops, functions, tx_instant)
    }
}

struct Normalizer<'a> {
    db_before: &'a Database,
    functions: &'a TxFunctions,
    next_anonymous: u64,
    primitive_count: usize,
}

impl Normalizer<'_> {
    fn expand_form(
        &mut self,
        form: &TxForm,
        depth: usize,
        output: &mut Vec<TxOp>,
    ) -> Result<(), SemanticError> {
        if depth > 32 {
            return Err(SemanticError::incorrect(
                "transaction/function-depth",
                "transaction function expansion exceeded 32 nested calls",
            ));
        }
        match form {
            TxForm::Op(op) => self.push(op.clone(), output),
            TxForm::EntityMap(map) => {
                self.expand_map(map, None, output)?;
                Ok(())
            }
            TxForm::Call(call) => {
                // Every call receives the original database value. Generated
                // calls recurse with that same value, never an intermediate DB.
                let mut generated = self.functions.invoke(self.db_before, call)?;
                generated.sort_by_key(|form| format!("{form:?}"));
                for generated in &generated {
                    self.expand_form(generated, depth + 1, output)?;
                }
                Ok(())
            }
        }
    }

    fn push(&mut self, op: TxOp, output: &mut Vec<TxOp>) -> Result<(), SemanticError> {
        self.primitive_count += 1;
        if self.primitive_count > 100_000 {
            return Err(SemanticError::incorrect(
                "transaction/expansion-limit",
                "transaction expansion exceeded 100000 primitive operations",
            ));
        }
        output.push(op);
        Ok(())
    }

    fn expand_map(
        &mut self,
        map: &EntityMap,
        forced_id: Option<EntityRef>,
        output: &mut Vec<TxOp>,
    ) -> Result<EntityRef, SemanticError> {
        let entity = forced_id.or_else(|| map.id.clone()).unwrap_or_else(|| {
            let id = EntityRef::Temp(format!("__map/{:020}", self.next_anonymous));
            self.next_anonymous += 1;
            id
        });
        let mut attributes = map.attributes.clone();
        attributes.sort_by_key(|entry| format!("{entry:?}"));
        for (attribute_ref, value) in &attributes {
            let (attribute_id, reverse) = self.resolve_attribute(attribute_ref)?;
            let attribute = self.db_before.schema().attribute(attribute_id)?;
            self.expand_map_value(
                entity.clone(),
                attribute_id,
                reverse,
                attribute.cardinality,
                attribute.value_type,
                attribute.component,
                value,
                output,
            )?;
        }
        Ok(entity)
    }

    #[allow(clippy::too_many_arguments)]
    fn expand_map_value(
        &mut self,
        owner: EntityRef,
        attribute: u32,
        reverse: bool,
        cardinality: Cardinality,
        value_type: ValueType,
        component: bool,
        value: &MapValue,
        output: &mut Vec<TxOp>,
    ) -> Result<(), SemanticError> {
        match value {
            MapValue::Many(values) => {
                if !reverse && cardinality != Cardinality::Many {
                    return Err(SemanticError::incorrect(
                        "transaction/collection-on-cardinality-one",
                        "a collection map value requires a cardinality-many attribute",
                    ));
                }
                let mut values = values.clone();
                values.sort_by_key(|value| format!("{value:?}"));
                for value in &values {
                    if matches!(value, MapValue::Many(_)) {
                        return Err(SemanticError::incorrect(
                            "transaction/nested-collection",
                            "map value collections cannot contain collections",
                        ));
                    }
                    self.expand_map_value(
                        owner.clone(),
                        attribute,
                        reverse,
                        cardinality,
                        value_type,
                        component,
                        value,
                        output,
                    )?;
                }
                Ok(())
            }
            MapValue::Value(value) => {
                if reverse {
                    let TxValue::Entity(source) = value else {
                        return Err(SemanticError::incorrect(
                            "transaction/reverse-value-must-be-entity",
                            "a reverse attribute value must identify an entity",
                        ));
                    };
                    self.push(
                        TxOp::Add {
                            entity: source.clone(),
                            attribute,
                            value: TxValue::Entity(owner),
                        },
                        output,
                    )
                } else {
                    self.push(
                        TxOp::Add {
                            entity: owner,
                            attribute,
                            value: value.clone(),
                        },
                        output,
                    )
                }
            }
            MapValue::Nested(nested) => {
                if value_type != ValueType::Ref {
                    return Err(SemanticError::incorrect(
                        "transaction/nested-map-requires-ref",
                        "nested maps require a ref-valued attribute",
                    ));
                }
                if reverse {
                    if !self.has_unique_attribute(nested)? {
                        return Err(SemanticError::incorrect(
                            "transaction/orphan-nested-map",
                            "a reverse nested map must include a unique attribute",
                        ));
                    }
                    let source = self.expand_map(nested, None, output)?;
                    self.push(
                        TxOp::Add {
                            entity: source,
                            attribute,
                            value: TxValue::Entity(owner),
                        },
                        output,
                    )
                } else {
                    if !component && !self.has_unique_attribute(nested)? {
                        return Err(SemanticError::incorrect(
                            "transaction/orphan-nested-map",
                            "a nested map requires a component edge or unique attribute",
                        ));
                    }
                    let child = self.expand_map(nested, None, output)?;
                    self.push(
                        TxOp::Add {
                            entity: owner,
                            attribute,
                            value: TxValue::Entity(child),
                        },
                        output,
                    )
                }
            }
        }
    }

    fn has_unique_attribute(&self, map: &EntityMap) -> Result<bool, SemanticError> {
        for (attribute, value) in &map.attributes {
            if matches!(value, MapValue::Many(_) | MapValue::Nested(_)) {
                continue;
            }
            let (attribute, reverse) = self.resolve_attribute(attribute)?;
            if !reverse
                && self
                    .db_before
                    .schema()
                    .attribute(attribute)?
                    .unique
                    .is_some_and(|unique| matches!(unique, Unique::Identity | Unique::Value))
            {
                return Ok(true);
            }
        }
        Ok(false)
    }

    fn resolve_attribute(&self, attribute: &AttributeRef) -> Result<(u32, bool), SemanticError> {
        match attribute {
            AttributeRef::Id(id) => Ok((*id, false)),
            AttributeRef::ReverseId(id) => Ok((*id, true)),
            AttributeRef::Ident(ident) | AttributeRef::ReverseIdent(ident) => self
                .db_before
                .schema()
                .resolve_ident(ident)
                .map(|id| (id, matches!(attribute, AttributeRef::ReverseIdent(_))))
                .ok_or_else(|| {
                    SemanticError::incorrect(
                        "schema/unknown-attribute",
                        format!("unknown attribute {}", ident.qualified_name()),
                    )
                }),
        }
    }
}
