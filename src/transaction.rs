use crate::{
    Attribute, CallableRef, Cardinality, Database, EntityRef, Keyword, ProgramCall, RuntimeValue,
    SemanticError, TupleSpec, TxOp, TxReport, TxValue, Unique, Value, ValueType,
};
use std::cmp::Ordering;
use std::collections::BTreeMap;
use std::fmt;
use std::panic::{AssertUnwindSafe, catch_unwind};
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
    /// A persisted/native database-function call. The authoritative
    /// transactor resolves this against the immutable db-before before the
    /// ordinary transaction normalizer runs.
    ProgramCall(ProgramCall),
    /// A process-local Rust callback used by speculative `with_forms` calls.
    /// This is deliberately distinct from a temporal database function.
    Call(TxCall),
}

/// Total structural order for primitive transaction forms.
///
/// Datomic's recovered implementation compares typed transaction values; it
/// does not make formatted diagnostic text part of transaction semantics. The
/// kernel additionally canonicalizes its declarative input so tempid and
/// anonymous-map allocation cannot depend on caller collection order. This
/// comparator is that native boundary: logical value order comes first and
/// representation details break ties (notably BigDecimal scale and numeric
/// representation).
pub(crate) fn compare_tx_op(left: &TxOp, right: &TxOp) -> Ordering {
    tx_op_rank(left)
        .cmp(&tx_op_rank(right))
        .then_with(|| match (left, right) {
            (
                TxOp::Add {
                    entity: left_entity,
                    attribute: left_attribute,
                    value: left_value,
                },
                TxOp::Add {
                    entity: right_entity,
                    attribute: right_attribute,
                    value: right_value,
                },
            ) => compare_entity_ref(left_entity, right_entity)
                .then_with(|| left_attribute.cmp(right_attribute))
                .then_with(|| compare_tx_value(left_value, right_value)),
            (
                TxOp::Retract {
                    entity: left_entity,
                    attribute: left_attribute,
                    value: left_value,
                },
                TxOp::Retract {
                    entity: right_entity,
                    attribute: right_attribute,
                    value: right_value,
                },
            ) => compare_entity_ref(left_entity, right_entity)
                .then_with(|| left_attribute.cmp(right_attribute))
                .then_with(|| compare_option_by(left_value, right_value, compare_tx_value)),
            (
                TxOp::Cas {
                    entity: left_entity,
                    attribute: left_attribute,
                    old: left_old,
                    new: left_new,
                },
                TxOp::Cas {
                    entity: right_entity,
                    attribute: right_attribute,
                    old: right_old,
                    new: right_new,
                },
            ) => compare_entity_ref(left_entity, right_entity)
                .then_with(|| left_attribute.cmp(right_attribute))
                .then_with(|| compare_option_by(left_old, right_old, compare_tx_value))
                .then_with(|| compare_tx_value(left_new, right_new)),
            (TxOp::RetractEntity(left), TxOp::RetractEntity(right)) => {
                compare_entity_ref(left, right)
            }
            (
                TxOp::Ensure {
                    entity: left_entity,
                    spec: left_spec,
                },
                TxOp::Ensure {
                    entity: right_entity,
                    spec: right_spec,
                },
            ) => compare_entity_ref(left_entity, right_entity)
                .then_with(|| compare_entity_ref(left_spec, right_spec)),
            (TxOp::InstallAttribute(left), TxOp::InstallAttribute(right))
            | (TxOp::AlterAttribute(left), TxOp::AlterAttribute(right)) => {
                compare_attribute(left, right)
            }
            _ => Ordering::Equal,
        })
}

fn tx_op_rank(op: &TxOp) -> u8 {
    match op {
        TxOp::Add { .. } => 0,
        TxOp::Retract { .. } => 1,
        TxOp::Cas { .. } => 2,
        TxOp::RetractEntity(_) => 3,
        TxOp::Ensure { .. } => 4,
        TxOp::InstallAttribute(_) => 5,
        TxOp::AlterAttribute(_) => 6,
    }
}

fn compare_tx_form(left: &TxForm, right: &TxForm) -> Ordering {
    tx_form_rank(left)
        .cmp(&tx_form_rank(right))
        .then_with(|| match (left, right) {
            (TxForm::Op(left), TxForm::Op(right)) => compare_tx_op(left, right),
            (TxForm::EntityMap(left), TxForm::EntityMap(right)) => compare_entity_map(left, right),
            (TxForm::ProgramCall(left), TxForm::ProgramCall(right)) => {
                compare_program_call(left, right)
            }
            (TxForm::Call(left), TxForm::Call(right)) => {
                compare_text(&left.function, &right.function).then_with(|| {
                    compare_slice_by(&left.arguments, &right.arguments, compare_tx_value)
                })
            }
            _ => Ordering::Equal,
        })
}

fn tx_form_rank(form: &TxForm) -> u8 {
    match form {
        TxForm::Op(_) => 0,
        TxForm::EntityMap(_) => 1,
        TxForm::ProgramCall(_) => 2,
        TxForm::Call(_) => 3,
    }
}

fn compare_program_call(left: &ProgramCall, right: &ProgramCall) -> Ordering {
    compare_callable_ref(&left.function, &right.function).then_with(|| {
        compare_slice_by(&left.arguments, &right.arguments, |left, right| {
            left.canonical_cmp(right)
        })
    })
}

fn compare_callable_ref(left: &CallableRef, right: &CallableRef) -> Ordering {
    callable_ref_rank(left)
        .cmp(&callable_ref_rank(right))
        .then_with(|| match (left, right) {
            (CallableRef::Database(left), CallableRef::Database(right)) => {
                compare_entity_ref(left, right)
            }
            (CallableRef::ExactHash(left), CallableRef::ExactHash(right)) => left.cmp(right),
            (CallableRef::Local(left), CallableRef::Local(right)) => left.cmp(right),
            _ => Ordering::Equal,
        })
}

fn callable_ref_rank(callable: &CallableRef) -> u8 {
    match callable {
        CallableRef::Database(_) => 0,
        CallableRef::ExactHash(_) => 1,
        CallableRef::Local(_) => 2,
    }
}

fn compare_entity_map(left: &EntityMap, right: &EntityMap) -> Ordering {
    compare_option_by(&left.id, &right.id, compare_entity_ref)
        .then_with(|| compare_map_entries(&left.attributes, &right.attributes))
}

fn compare_map_entries(
    left: &[(AttributeRef, MapValue)],
    right: &[(AttributeRef, MapValue)],
) -> Ordering {
    let mut left: Vec<_> = left.iter().collect();
    let mut right: Vec<_> = right.iter().collect();
    left.sort_by(|left, right| compare_map_entry(left, right));
    right.sort_by(|left, right| compare_map_entry(left, right));
    compare_slice_by(&left, &right, |left, right| compare_map_entry(left, right))
}

fn compare_map_entry(
    left: &(AttributeRef, MapValue),
    right: &(AttributeRef, MapValue),
) -> Ordering {
    compare_attribute_ref(&left.0, &right.0).then_with(|| compare_map_value(&left.1, &right.1))
}

fn compare_attribute_ref(left: &AttributeRef, right: &AttributeRef) -> Ordering {
    attribute_ref_rank(left)
        .cmp(&attribute_ref_rank(right))
        .then_with(|| match (left, right) {
            (AttributeRef::Id(left), AttributeRef::Id(right))
            | (AttributeRef::ReverseId(left), AttributeRef::ReverseId(right)) => left.cmp(right),
            (AttributeRef::Ident(left), AttributeRef::Ident(right))
            | (AttributeRef::ReverseIdent(left), AttributeRef::ReverseIdent(right)) => {
                compare_keyword(left, right)
            }
            _ => Ordering::Equal,
        })
}

fn attribute_ref_rank(attribute: &AttributeRef) -> u8 {
    match attribute {
        AttributeRef::Id(_) => 0,
        AttributeRef::Ident(_) => 1,
        AttributeRef::ReverseId(_) => 2,
        AttributeRef::ReverseIdent(_) => 3,
    }
}

fn compare_map_value(left: &MapValue, right: &MapValue) -> Ordering {
    map_value_rank(left)
        .cmp(&map_value_rank(right))
        .then_with(|| match (left, right) {
            (MapValue::Value(left), MapValue::Value(right)) => compare_tx_value(left, right),
            (MapValue::Nested(left), MapValue::Nested(right)) => compare_entity_map(left, right),
            (MapValue::Many(left), MapValue::Many(right)) => {
                compare_unordered_map_values(left, right)
            }
            _ => Ordering::Equal,
        })
}

fn map_value_rank(value: &MapValue) -> u8 {
    match value {
        MapValue::Value(_) => 0,
        MapValue::Nested(_) => 1,
        MapValue::Many(_) => 2,
    }
}

fn compare_unordered_map_values(left: &[MapValue], right: &[MapValue]) -> Ordering {
    let mut left: Vec<_> = left.iter().collect();
    let mut right: Vec<_> = right.iter().collect();
    left.sort_by(|left, right| compare_map_value(left, right));
    right.sort_by(|left, right| compare_map_value(left, right));
    compare_slice_by(&left, &right, |left, right| compare_map_value(left, right))
}

fn compare_entity_ref(left: &EntityRef, right: &EntityRef) -> Ordering {
    entity_ref_rank(left)
        .cmp(&entity_ref_rank(right))
        .then_with(|| match (left, right) {
            (EntityRef::Id(left), EntityRef::Id(right)) => left.cmp(right),
            (EntityRef::Ident(left), EntityRef::Ident(right)) => compare_keyword(left, right),
            (EntityRef::Temp(left), EntityRef::Temp(right)) => compare_text(left, right),
            (
                EntityRef::Lookup {
                    attribute: left_attribute,
                    value: left_value,
                },
                EntityRef::Lookup {
                    attribute: right_attribute,
                    value: right_value,
                },
            ) => left_attribute
                .cmp(right_attribute)
                .then_with(|| compare_value(left_value, right_value)),
            (EntityRef::Tx, EntityRef::Tx) => Ordering::Equal,
            _ => Ordering::Equal,
        })
}

fn entity_ref_rank(entity: &EntityRef) -> u8 {
    match entity {
        EntityRef::Id(_) => 0,
        EntityRef::Ident(_) => 1,
        EntityRef::Temp(_) => 2,
        EntityRef::Lookup { .. } => 3,
        EntityRef::Tx => 4,
    }
}

fn compare_tx_value(left: &TxValue, right: &TxValue) -> Ordering {
    tx_value_rank(left)
        .cmp(&tx_value_rank(right))
        .then_with(|| match (left, right) {
            (TxValue::Scalar(left), TxValue::Scalar(right)) => compare_value(left, right),
            (TxValue::Entity(left), TxValue::Entity(right)) => compare_entity_ref(left, right),
            _ => Ordering::Equal,
        })
}

fn tx_value_rank(value: &TxValue) -> u8 {
    match value {
        TxValue::Scalar(_) => 0,
        TxValue::Entity(_) => 1,
    }
}

fn compare_value(left: &Value, right: &Value) -> Ordering {
    left.stored_cmp(right)
        .then_with(|| value_rank(left).cmp(&value_rank(right)))
        .then_with(|| match (left, right) {
            (Value::BigDec(left), Value::BigDec(right)) => left
                .as_bigint_and_exponent()
                .cmp(&right.as_bigint_and_exponent()),
            (Value::BigInt(left), Value::BigInt(right)) => left.cmp(right),
            (Value::Bool(left), Value::Bool(right)) => left.cmp(right),
            (Value::Bytes(left), Value::Bytes(right)) => left.cmp(right),
            (Value::Double(left), Value::Double(right)) => left.total_cmp(right),
            (Value::Float(left), Value::Float(right)) => left.total_cmp(right),
            (Value::Function(left), Value::Function(right)) => left.cmp(right),
            (Value::Instant(left), Value::Instant(right)) => left.cmp(right),
            (Value::Keyword(left), Value::Keyword(right)) => compare_keyword(left, right),
            (Value::Long(left), Value::Long(right)) => left.cmp(right),
            (Value::Ref(left), Value::Ref(right)) => left.cmp(right),
            (Value::String(left), Value::String(right)) => compare_text(left, right),
            (Value::Symbol(left), Value::Symbol(right)) => compare_names(
                left.namespace.as_deref(),
                &left.name,
                right.namespace.as_deref(),
                &right.name,
            ),
            (Value::Tuple(left), Value::Tuple(right)) => {
                compare_slice_by(left, right, |left, right| {
                    compare_option_by(left, right, compare_value)
                })
            }
            (Value::Uuid(left), Value::Uuid(right)) => left.cmp(right),
            (Value::Uri(left), Value::Uri(right)) => compare_text(left, right),
            _ => Ordering::Equal,
        })
}

fn value_rank(value: &Value) -> u8 {
    match value {
        Value::BigDec(_) => 0,
        Value::BigInt(_) => 1,
        Value::Bool(_) => 2,
        Value::Bytes(_) => 3,
        Value::Double(_) => 4,
        Value::Float(_) => 5,
        Value::Instant(_) => 6,
        Value::Keyword(_) => 7,
        Value::Long(_) => 8,
        Value::Ref(_) => 9,
        Value::String(_) => 10,
        Value::Symbol(_) => 11,
        Value::Tuple(_) => 12,
        Value::Uuid(_) => 13,
        Value::Uri(_) => 14,
        Value::Function(_) => 15,
    }
}

fn compare_attribute(left: &Attribute, right: &Attribute) -> Ordering {
    left.id
        .cmp(&right.id)
        .then_with(|| compare_keyword(&left.ident, &right.ident))
        .then_with(|| value_type_rank(left.value_type).cmp(&value_type_rank(right.value_type)))
        .then_with(|| cardinality_rank(left.cardinality).cmp(&cardinality_rank(right.cardinality)))
        .then_with(|| {
            compare_option_by(&left.unique, &right.unique, |left, right| {
                unique_rank(*left).cmp(&unique_rank(*right))
            })
        })
        .then_with(|| left.indexed.cmp(&right.indexed))
        .then_with(|| left.component.cmp(&right.component))
        .then_with(|| left.no_history.cmp(&right.no_history))
        .then_with(|| compare_option_by(&left.tuple, &right.tuple, compare_tuple_spec))
        .then_with(|| left.tuple_discontinued.cmp(&right.tuple_discontinued))
        .then_with(|| compare_unordered_text(&left.predicates, &right.predicates))
}

fn compare_tuple_spec(left: &TupleSpec, right: &TupleSpec) -> Ordering {
    tuple_spec_rank(left)
        .cmp(&tuple_spec_rank(right))
        .then_with(|| match (left, right) {
            (TupleSpec::Homogeneous(left), TupleSpec::Homogeneous(right)) => {
                value_type_rank(*left).cmp(&value_type_rank(*right))
            }
            (TupleSpec::Heterogeneous(left), TupleSpec::Heterogeneous(right)) => {
                compare_slice_by(left, right, |left, right| {
                    value_type_rank(*left).cmp(&value_type_rank(*right))
                })
            }
            (TupleSpec::Composite(left), TupleSpec::Composite(right)) => left.cmp(right),
            _ => Ordering::Equal,
        })
}

fn tuple_spec_rank(spec: &TupleSpec) -> u8 {
    match spec {
        TupleSpec::Homogeneous(_) => 0,
        TupleSpec::Heterogeneous(_) => 1,
        TupleSpec::Composite(_) => 2,
    }
}

fn value_type_rank(value_type: ValueType) -> u8 {
    match value_type {
        ValueType::BigDec => 0,
        ValueType::BigInt => 1,
        ValueType::Boolean => 2,
        ValueType::Bytes => 3,
        ValueType::Double => 4,
        ValueType::Float => 5,
        ValueType::Instant => 6,
        ValueType::Keyword => 7,
        ValueType::Long => 8,
        ValueType::Ref => 9,
        ValueType::String => 10,
        ValueType::Symbol => 11,
        ValueType::Tuple => 12,
        ValueType::Uuid => 13,
        ValueType::Uri => 14,
        ValueType::Function => 15,
    }
}

fn cardinality_rank(cardinality: Cardinality) -> u8 {
    match cardinality {
        Cardinality::One => 0,
        Cardinality::Many => 1,
    }
}

fn unique_rank(unique: Unique) -> u8 {
    match unique {
        Unique::Identity => 0,
        Unique::Value => 1,
    }
}

fn compare_keyword(left: &Keyword, right: &Keyword) -> Ordering {
    compare_names(
        left.namespace.as_deref(),
        &left.name,
        right.namespace.as_deref(),
        &right.name,
    )
}

fn compare_names(
    left_namespace: Option<&str>,
    left_name: &str,
    right_namespace: Option<&str>,
    right_name: &str,
) -> Ordering {
    compare_option_by(&left_namespace, &right_namespace, |left, right| {
        compare_text(left, right)
    })
    .then_with(|| compare_text(left_name, right_name))
}

fn compare_text(left: &str, right: &str) -> Ordering {
    left.encode_utf16().cmp(right.encode_utf16())
}

fn compare_unordered_text(left: &[String], right: &[String]) -> Ordering {
    let mut left: Vec<_> = left.iter().map(String::as_str).collect();
    let mut right: Vec<_> = right.iter().map(String::as_str).collect();
    left.sort_by(|left, right| compare_text(left, right));
    right.sort_by(|left, right| compare_text(left, right));
    compare_slice_by(&left, &right, |left, right| compare_text(left, right))
}

fn compare_option_by<T, F>(left: &Option<T>, right: &Option<T>, compare: F) -> Ordering
where
    F: FnOnce(&T, &T) -> Ordering,
{
    match (left, right) {
        (None, None) => Ordering::Equal,
        (None, Some(_)) => Ordering::Less,
        (Some(_), None) => Ordering::Greater,
        (Some(left), Some(right)) => compare(left, right),
    }
}

fn compare_slice_by<T, F>(left: &[T], right: &[T], mut compare: F) -> Ordering
where
    F: FnMut(&T, &T) -> Ordering,
{
    for (left, right) in left.iter().zip(right) {
        let ordering = compare(left, right);
        if ordering != Ordering::Equal {
            return ordering;
        }
    }
    left.len().cmp(&right.len())
}

type NativeFunction =
    dyn Fn(&Database, &[TxValue]) -> Result<Vec<TxForm>, SemanticError> + Send + Sync;
type NativePredicate = dyn Fn(&crate::Value) -> Result<RuntimeValue, SemanticError> + Send + Sync;
type NativeEntityPredicate =
    dyn Fn(&Database, u64) -> Result<RuntimeValue, SemanticError> + Send + Sync;

/// Process-local deterministic transaction functions.
///
/// Persisted/sandboxed functions are deliberately not represented here. This
/// boundary proves the documented `[db-before, args] -> tx-data` semantics
/// without importing JVM classpath behavior into the native kernel.
#[derive(Clone, Default)]
pub struct TxFunctions {
    functions: BTreeMap<String, Arc<NativeFunction>>,
    predicates: BTreeMap<String, Arc<NativePredicate>>,
    entity_predicates: BTreeMap<String, Arc<NativeEntityPredicate>>,
}

impl fmt::Debug for TxFunctions {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("TxFunctions")
            .field("names", &self.functions.keys().collect::<Vec<_>>())
            .field("predicates", &self.predicates.keys().collect::<Vec<_>>())
            .field(
                "entity_predicates",
                &self.entity_predicates.keys().collect::<Vec<_>>(),
            )
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
        self.predicates.insert(
            name.into(),
            Arc::new(move |value| {
                predicate(value).map(|value| RuntimeValue::Scalar(Value::Bool(value)))
            }),
        );
    }

    /// Register a predicate whose exact returned value is semantically
    /// observable. Datomic accepts only literal true and retains any other
    /// result in the rejection data; persisted programs use this boundary.
    pub(crate) fn register_attribute_value_predicate<F>(
        &mut self,
        name: impl Into<String>,
        predicate: F,
    ) where
        F: Fn(&crate::Value) -> Result<RuntimeValue, SemanticError> + Send + Sync + 'static,
    {
        self.predicates.insert(name.into(), Arc::new(predicate));
    }

    /// Register an entity-spec predicate. The recovered `ensure-entity!`
    /// boundary invokes these with the complete proposed db-after and the
    /// resolved entity id, while the spec itself was resolved in db-before.
    pub fn register_entity_predicate<F>(&mut self, name: impl Into<String>, predicate: F)
    where
        F: Fn(&Database, u64) -> Result<bool, SemanticError> + Send + Sync + 'static,
    {
        self.entity_predicates.insert(
            name.into(),
            Arc::new(move |database, entity| {
                predicate(database, entity).map(|value| RuntimeValue::Scalar(Value::Bool(value)))
            }),
        );
    }

    pub(crate) fn register_entity_value_predicate<F>(
        &mut self,
        name: impl Into<String>,
        predicate: F,
    ) where
        F: Fn(&Database, u64) -> Result<RuntimeValue, SemanticError> + Send + Sync + 'static,
    {
        self.entity_predicates
            .insert(name.into(), Arc::new(predicate));
    }

    pub(crate) fn validate_attribute_predicate(
        &self,
        name: &str,
        value: &crate::Value,
    ) -> Result<RuntimeValue, SemanticError> {
        let predicate = self.predicates.get(name).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-attribute-predicate",
                format!("unknown attribute predicate {name}"),
            )
        })?;
        catch_unwind(AssertUnwindSafe(|| predicate(value))).map_err(|_| {
            SemanticError::new(
                crate::ErrorCategory::Fault,
                "transaction/predicate-panic",
                format!("attribute predicate {name} panicked"),
            )
        })?
    }

    pub(crate) fn validate_entity_predicate(
        &self,
        name: &str,
        db_after: &Database,
        entity: u64,
    ) -> Result<RuntimeValue, SemanticError> {
        let predicate = self.entity_predicates.get(name).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-entity-predicate",
                format!("unknown entity predicate {name}"),
            )
        })?;
        catch_unwind(AssertUnwindSafe(|| predicate(db_after, entity))).map_err(|_| {
            SemanticError::new(
                crate::ErrorCategory::Fault,
                "transaction/predicate-panic",
                format!("entity predicate {name} panicked"),
            )
        })?
    }

    fn invoke(&self, db_before: &Database, call: &TxCall) -> Result<Vec<TxForm>, SemanticError> {
        let function = self.functions.get(&call.function).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-function",
                format!("unknown transaction function {}", call.function),
            )
        })?;
        catch_unwind(AssertUnwindSafe(|| function(db_before, &call.arguments))).map_err(|_| {
            SemanticError::new(
                crate::ErrorCategory::Fault,
                "transaction/function-panic",
                format!("transaction function {} panicked", call.function),
            )
        })?
    }
}

impl Database {
    /// Expand structured and process-local transaction forms against this one
    /// immutable db-before without applying them. Persisted database calls are
    /// deliberately resolved by the authoritative transactor first.
    pub(crate) fn normalize_forms(
        &self,
        forms: &[TxForm],
        functions: &TxFunctions,
    ) -> Result<Vec<TxOp>, SemanticError> {
        self.normalize_forms_with_limit(forms, functions, usize::MAX)
    }

    pub(crate) fn normalize_forms_with_limit(
        &self,
        forms: &[TxForm],
        functions: &TxFunctions,
        max_primitive_ops: usize,
    ) -> Result<Vec<TxOp>, SemanticError> {
        let mut normalizer = Normalizer {
            db_before: self,
            functions,
            next_anonymous: 0,
            primitive_count: 0,
            max_primitive_ops,
        };
        let mut forms = forms.to_vec();
        forms.sort_by(compare_tx_form);
        let mut ops = Vec::new();
        for form in &forms {
            normalizer.expand_form(form, 0, &mut ops)?;
        }
        Ok(ops)
    }

    /// Normalize map and function forms against one db-before, then apply the
    /// resulting complete primitive information set through `with`.
    pub fn with_forms(
        &self,
        forms: &[TxForm],
        functions: &TxFunctions,
        tx_instant: i64,
    ) -> Result<TxReport, SemanticError> {
        let ops = self.normalize_forms(forms, functions)?;
        self.with_function_context(&ops, functions, tx_instant)
    }
}

struct Normalizer<'a> {
    db_before: &'a Database,
    functions: &'a TxFunctions,
    next_anonymous: u64,
    primitive_count: usize,
    max_primitive_ops: usize,
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
                self.expand_map(map, None, 0, output)?;
                Ok(())
            }
            TxForm::ProgramCall(_) => Err(SemanticError::incorrect(
                "transaction/unresolved-database-function",
                "persisted database-function calls must be resolved against db-before by the transactor",
            )),
            TxForm::Call(call) => {
                // Every call receives the original database value. Generated
                // calls recurse with that same value, never an intermediate DB.
                let mut generated = self.functions.invoke(self.db_before, call)?;
                generated.sort_by(compare_tx_form);
                for generated in &generated {
                    self.expand_form(generated, depth + 1, output)?;
                }
                Ok(())
            }
        }
    }

    fn push(&mut self, op: TxOp, output: &mut Vec<TxOp>) -> Result<(), SemanticError> {
        self.primitive_count = self.primitive_count.checked_add(1).ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::Busy,
                "transaction/expansion-overflow",
                "transaction expansion operation count overflowed",
            )
        })?;
        if self.primitive_count > self.max_primitive_ops {
            return Err(SemanticError::new(
                crate::ErrorCategory::Busy,
                "postgres/transaction-op-capacity",
                format!(
                    "transaction expansion exceeds the configured {}-operation limit",
                    self.max_primitive_ops
                ),
            ));
        }
        output.push(op);
        Ok(())
    }

    fn expand_map(
        &mut self,
        map: &EntityMap,
        forced_id: Option<EntityRef>,
        depth: usize,
        output: &mut Vec<TxOp>,
    ) -> Result<EntityRef, SemanticError> {
        if depth > 32 {
            return Err(SemanticError::incorrect(
                "transaction/map-depth",
                "transaction entity maps may contain at most 32 nested maps",
            ));
        }
        let entity = forced_id.or_else(|| map.id.clone()).unwrap_or_else(|| {
            let id = EntityRef::Temp(format!("__map/{:020}", self.next_anonymous));
            self.next_anonymous += 1;
            id
        });
        let mut attributes = map.attributes.clone();
        attributes.sort_by(compare_map_entry);
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
                depth,
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
        depth: usize,
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
                values.sort_by(compare_map_value);
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
                        depth,
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
                    let source = self.expand_map(nested, None, depth + 1, output)?;
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
                    let child = self.expand_map(nested, None, depth + 1, output)?;
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
            AttributeRef::Ident(ident) | AttributeRef::ReverseIdent(ident) => {
                let entity = self.db_before.entid(ident).ok_or_else(|| {
                    SemanticError::incorrect(
                        "schema/unknown-attribute",
                        format!("unknown attribute {}", ident.qualified_name()),
                    )
                })?;
                let id = crate::schema_eid_to_attr_id(entity)?;
                self.db_before.schema().attribute(id)?;
                Ok((id, matches!(attribute, AttributeRef::ReverseIdent(_))))
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use bigdecimal::BigDecimal;
    use std::str::FromStr;

    fn scalar(value: Value) -> MapValue {
        MapValue::Value(TxValue::Scalar(value))
    }

    #[test]
    fn map_and_many_collections_have_an_order_independent_structural_key() {
        let left = EntityMap {
            id: None,
            attributes: vec![
                (
                    AttributeRef::Id(12),
                    MapValue::Many(vec![
                        scalar(Value::String("z".into())),
                        scalar(Value::String("a".into())),
                    ]),
                ),
                (
                    AttributeRef::Ident(Keyword::new("person", "name")),
                    scalar(Value::String("Ada".into())),
                ),
            ],
        };
        let right = EntityMap {
            id: None,
            attributes: vec![
                (
                    AttributeRef::Ident(Keyword::new("person", "name")),
                    scalar(Value::String("Ada".into())),
                ),
                (
                    AttributeRef::Id(12),
                    MapValue::Many(vec![
                        scalar(Value::String("a".into())),
                        scalar(Value::String("z".into())),
                    ]),
                ),
            ],
        };

        assert_eq!(compare_entity_map(&left, &right), Ordering::Equal);
        assert_eq!(compare_entity_map(&right, &left), Ordering::Equal);

        let op = TxForm::Op(TxOp::RetractEntity(EntityRef::Tx));
        let mut first = vec![TxForm::EntityMap(left), op.clone()];
        let mut second = vec![op, TxForm::EntityMap(right)];
        first.sort_by(compare_tx_form);
        second.sort_by(compare_tx_form);
        assert!(
            first
                .iter()
                .zip(&second)
                .all(|(left, right)| compare_tx_form(left, right) == Ordering::Equal)
        );
    }

    #[test]
    fn structural_value_ties_preserve_stored_numeric_representations() {
        let one_scale = Value::BigDec(BigDecimal::from_str("1.0").unwrap());
        let two_scale = Value::BigDec(BigDecimal::from_str("1.00").unwrap());
        assert_eq!(one_scale.index_cmp(&two_scale), Ordering::Equal);
        assert_ne!(compare_value(&one_scale, &two_scale), Ordering::Equal);
        assert_eq!(
            compare_value(&one_scale, &two_scale),
            compare_value(&two_scale, &one_scale).reverse()
        );

        let integer = Value::Long(1);
        assert_eq!(integer.index_cmp(&one_scale), Ordering::Equal);
        assert_ne!(compare_value(&integer, &one_scale), Ordering::Equal);

        let negative_zero = Value::Double(-0.0);
        let positive_zero = Value::Double(0.0);
        assert_eq!(negative_zero.index_cmp(&positive_zero), Ordering::Equal);
        assert_ne!(
            compare_value(&negative_zero, &positive_zero),
            Ordering::Equal
        );
    }
}
