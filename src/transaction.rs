use crate::{
    Attribute, CallableRef, Cardinality, Database, DatabaseValue, EntityRef, Keyword, ProgramCall,
    RuntimeValue, SemanticError, TupleSpec, TxOp, TxReport, TxValue, Unique, Value, ValueType,
};

#[path = "transaction_input.rs"]
mod input;
pub(crate) use input::{
    validate_entity_input, validate_forms_input, validate_ops_input, validate_stored_input,
    validate_value_input,
};
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
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
            (
                TxOp::ForcePartition {
                    tempid: left,
                    partition: left_ref,
                },
                TxOp::ForcePartition {
                    tempid: right,
                    partition: right_ref,
                },
            )
            | (
                TxOp::MatchPartition {
                    tempid: left,
                    entity: left_ref,
                },
                TxOp::MatchPartition {
                    tempid: right,
                    entity: right_ref,
                },
            ) => compare_text(left, right).then_with(|| compare_entity_ref(left_ref, right_ref)),
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
        TxOp::ForcePartition { .. } => 7,
        TxOp::MatchPartition { .. } => 8,
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
            (
                EntityRef::LookupInput {
                    attribute: left_attribute,
                    value: left,
                },
                EntityRef::LookupInput {
                    attribute: right_attribute,
                    value: right,
                },
            ) => left_attribute
                .cmp(right_attribute)
                .then_with(|| compare_tx_value(left, right)),
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
        EntityRef::LookupInput { .. } => 5,
    }
}

fn compare_tx_value(left: &TxValue, right: &TxValue) -> Ordering {
    tx_value_rank(left)
        .cmp(&tx_value_rank(right))
        .then_with(|| match (left, right) {
            (TxValue::Scalar(left), TxValue::Scalar(right)) => compare_value(left, right),
            (TxValue::Entity(left), TxValue::Entity(right)) => compare_entity_ref(left, right),
            (TxValue::Tuple(left), TxValue::Tuple(right)) => {
                compare_slice_by(left, right, |left, right| {
                    compare_option_by(left, right, compare_tx_value)
                })
            }
            _ => Ordering::Equal,
        })
}

fn tx_value_rank(value: &TxValue) -> u8 {
    match value {
        TxValue::Scalar(_) => 0,
        TxValue::Entity(_) => 1,
        TxValue::Tuple(_) => 2,
    }
}

/// New-input grammar is selected only when needed, preserving all existing
/// request hashes rather than globally bumping the idempotency domain.
pub(crate) fn forms_have_extended_inputs(forms: &[TxForm]) -> bool {
    fn entity(input: &EntityRef) -> bool {
        matches!(input, EntityRef::LookupInput { .. })
    }
    fn value(value: &TxValue) -> bool {
        matches!(
            value,
            TxValue::Tuple(_) | TxValue::Entity(EntityRef::LookupInput { .. })
        )
    }
    fn map(input: &EntityMap) -> bool {
        input.id.as_ref().is_some_and(entity)
            || input.attributes.iter().any(|(_, input)| map_value(input))
    }
    fn map_value(input: &MapValue) -> bool {
        match input {
            MapValue::Value(input) => value(input),
            MapValue::Nested(input) => map(input),
            MapValue::Many(inputs) => inputs.iter().any(map_value),
        }
    }
    forms.iter().any(|form| match form {
        TxForm::Op(TxOp::Add {
            entity: id,
            value: input,
            ..
        }) => entity(id) || value(input),
        TxForm::Op(TxOp::Retract {
            entity: id,
            value: input,
            ..
        }) => entity(id) || input.as_ref().is_some_and(value),
        TxForm::Op(TxOp::Cas {
            entity: id,
            old,
            new,
            ..
        }) => entity(id) || old.as_ref().is_some_and(value) || value(new),
        TxForm::Op(TxOp::RetractEntity(id)) => entity(id),
        TxForm::Op(TxOp::Ensure { entity: id, spec }) => entity(id) || entity(spec),
        TxForm::Op(TxOp::ForcePartition { partition, .. }) => entity(partition),
        TxForm::Op(TxOp::MatchPartition { entity: id, .. }) => entity(id),
        TxForm::EntityMap(input) => map(input),
        TxForm::ProgramCall(call) => {
            matches!(&call.function, CallableRef::Database(input) if entity(input))
                || call.arguments.iter().any(runtime_has_extended_inputs)
        }
        TxForm::Call(call) => call.arguments.iter().any(value),
        _ => false,
    })
}

pub(crate) fn forms_have_partition_directives(forms: &[TxForm]) -> bool {
    forms.iter().any(|form| {
        matches!(
            form,
            TxForm::Op(TxOp::ForcePartition { .. } | TxOp::MatchPartition { .. })
        )
    })
}

pub(crate) fn runtime_has_extended_inputs(input: &RuntimeValue) -> bool {
    match input {
        RuntimeValue::Entity(EntityRef::LookupInput { .. }) => true,
        RuntimeValue::Vector(values) => values.iter().any(runtime_has_extended_inputs),
        RuntimeValue::Map(values) => values
            .iter()
            .any(|(_, input)| runtime_has_extended_inputs(input)),
        _ => false,
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
        .then_with(|| left.fulltext.cmp(&right.fulltext))
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
type ExactEntityPredicate =
    dyn Fn(&DatabaseValue, u64) -> Result<RuntimeValue, SemanticError> + Send + Sync;

#[derive(Clone)]
enum RegisteredEntityPredicate {
    Eager(Arc<NativeEntityPredicate>),
    Exact(Arc<ExactEntityPredicate>),
}

/// Process-local deterministic transaction functions.
///
/// Persisted/sandboxed functions are deliberately not represented here. This
/// boundary proves the documented `[db-before, args] -> tx-data` semantics
/// without importing JVM classpath behavior into the native kernel.
#[derive(Clone, Default)]
pub struct TxFunctions {
    functions: BTreeMap<String, Arc<NativeFunction>>,
    predicates: BTreeMap<String, Arc<NativePredicate>>,
    entity_predicates: BTreeMap<String, RegisteredEntityPredicate>,
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
            RegisteredEntityPredicate::Eager(Arc::new(move |database, entity| {
                predicate(database, entity).map(|value| RuntimeValue::Scalar(Value::Bool(value)))
            })),
        );
    }

    pub(crate) fn register_entity_value_predicate<F>(
        &mut self,
        name: impl Into<String>,
        predicate: F,
    ) where
        F: Fn(&DatabaseValue, u64) -> Result<RuntimeValue, SemanticError> + Send + Sync + 'static,
    {
        self.entity_predicates.insert(
            name.into(),
            RegisteredEntityPredicate::Exact(Arc::new(predicate)),
        );
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
        catch_unwind(AssertUnwindSafe(|| match predicate {
            RegisteredEntityPredicate::Eager(predicate) => predicate(db_after, entity),
            RegisteredEntityPredicate::Exact(predicate) => {
                predicate(&db_after.database_value(), entity)
            }
        }))
        .map_err(|_| entity_predicate_panic(name))?
    }

    /// Validate a persisted entity predicate against the exact proposed
    /// db-after without materializing it. Process-local Rust callbacks retain
    /// their historical `&Database` API and therefore remain intentionally
    /// confined to the eager speculative kernel.
    #[allow(dead_code)] // consumed by the bounded assessor introduced in the next Goal 16 step
    pub(crate) fn validate_entity_predicate_exact(
        &self,
        name: &str,
        db_after: &DatabaseValue,
        entity: u64,
    ) -> Result<RuntimeValue, SemanticError> {
        let predicate = self.entity_predicates.get(name).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-entity-predicate",
                format!("unknown entity predicate {name}"),
            )
        })?;
        let RegisteredEntityPredicate::Exact(predicate) = predicate else {
            return Err(SemanticError::incorrect(
                "transaction/eager-entity-predicate",
                "process-local entity predicates require the eager speculative Database API",
            ));
        };
        catch_unwind(AssertUnwindSafe(|| predicate(db_after, entity)))
            .map_err(|_| entity_predicate_panic(name))?
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

fn entity_predicate_panic(name: &str) -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Fault,
        "transaction/predicate-panic",
        format!("entity predicate {name} panicked"),
    )
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
        normalize_forms_against(
            NormalizerRead::Eager(self),
            forms,
            Some(functions),
            max_primitive_ops,
        )
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

impl DatabaseValue {
    /// Apply primitive transaction information without persisting it. The
    /// caller supplies time, as for the eager `Database::with` oracle.
    pub fn with(
        &self,
        ops: &[TxOp],
        tx_instant: i64,
    ) -> Result<crate::SpeculativeTransactionReport, SemanticError> {
        validate_ops_input(ops)?;
        self.with_forms(
            &ops.iter().cloned().map(TxForm::Op).collect::<Vec<_>>(),
            tx_instant,
        )
    }

    /// Pure native transaction forms, including persisted controlled calls.
    /// All initial and generated calls see this same immutable db-before;
    /// predicates use the same validation path as committed transactions.
    /// Read filters (including as-of/since) are applied to the result, not to
    /// transaction generation or invariants. History values cannot transact.
    pub fn with_forms(
        &self,
        forms: &[TxForm],
        tx_instant: i64,
    ) -> Result<crate::SpeculativeTransactionReport, SemanticError> {
        self.with_forms_with_limits(forms, tx_instant, SpeculationLimits::default())
    }

    pub fn with_forms_with_limits(
        &self,
        forms: &[TxForm],
        tx_instant: i64,
        limits: SpeculationLimits,
    ) -> Result<crate::SpeculativeTransactionReport, SemanticError> {
        use crate::database_value::TransactionReadContext;
        use crate::postgres::program_bindings::{
            expand_submission_forms, persisted_predicates, transaction_program_roots,
            validate_successor_program_bindings, visit_program_closure,
        };
        use std::sync::Mutex;
        validate_forms_input(forms)?;
        if limits.max_operations == 0
            || limits.max_read_datoms == 0
            || limits.max_read_bytes == 0
            || limits.max_program_dependencies == 0
            || limits.max_program_bytes == 0
        {
            return Err(SemanticError::incorrect(
                "transaction/invalid-speculation-capacity",
                "speculative operation, read and code dependency limits must be positive",
            ));
        }
        let context = Arc::new(TransactionReadContext::new(
            limits.max_read_datoms,
            limits.max_read_bytes,
        ));
        let before = self
            .speculation_base()?
            .with_transaction_read_context(Arc::clone(&context));
        let budget = Arc::new(Mutex::new(crate::ProgramBudget::new(
            limits.program.control(),
        )?));
        let mut retained = BTreeMap::new();
        let mut program_bytes = 0usize;
        let mut resolve = |hash| {
            if let Some(program) = retained.get(&hash) {
                return Ok(Arc::clone(program));
            }
            if retained.len() >= limits.max_program_dependencies {
                return Err(SemanticError::new(
                    crate::ErrorCategory::Busy,
                    "transaction/program-dependency-capacity",
                    "speculative attempt exceeds its immutable code dependency count",
                ));
            }
            let program = self.resolve_program(hash)?;
            // Same canonical-payload + fixed-overhead proxy as the durable
            // program cache. At most one bounded blob is decoded before the
            // byte limit rejects it; dependency traversal never escapes this
            // resolver, including dormant fixed calls in newly bound code.
            let weight = crate::encode_program(program.program())?
                .len()
                .saturating_add(1_024 + std::mem::size_of::<crate::ProgramHash>());
            program_bytes = program_bytes
                .checked_add(weight)
                .filter(|bytes| *bytes <= limits.max_program_bytes)
                .ok_or_else(|| {
                    SemanticError::new(
                        crate::ErrorCategory::Busy,
                        "transaction/program-dependency-capacity",
                        "speculative attempt exceeds its immutable code byte allowance",
                    )
                })?;
            retained.insert(hash, Arc::clone(&program));
            Ok(program)
        };
        let expanded = {
            let mut budget = budget.lock().map_err(|_| {
                SemanticError::new(
                    crate::ErrorCategory::Fault,
                    "program/budget-poisoned",
                    "speculation budget mutex poisoned",
                )
            })?;
            expand_submission_forms(&mut resolve, &before, forms, &mut budget)?
        };
        let ops = before.normalize_persisted_forms_with_limit(&expanded, limits.max_operations)?;
        let remaining = context.remaining()?;
        let assessed = crate::tiered_assessor::assess_tiered_with_remaining_limits(
            &before,
            &ops,
            tx_instant,
            crate::tiered_assessor::AssessmentLimits {
                max_read_datoms: remaining.datoms,
                max_read_bytes: remaining.retained_bytes,
            },
        )?;
        validate_successor_program_bindings(
            &mut resolve,
            &assessed.db_before,
            &assessed.db_after,
            &assessed.tx_data,
        )?;
        let functions = persisted_predicates(
            &mut resolve,
            &assessed.db_before,
            &assessed.predicate_requirements()?,
            Arc::clone(&budget),
        )?;
        assessed.validate_exact(Some(&functions))?;
        visit_program_closure(
            &mut resolve,
            transaction_program_roots(&assessed.tx_data),
            &mut |_, _| {},
        )?;
        Ok(crate::SpeculativeTransactionReport {
            db_before: self.clone(),
            db_after: assessed
                .db_after
                .without_transaction_read_context()
                .retain_programs(retained)
                .with_speculation_view(self),
            tx_data: assessed.tx_data,
            tempids: assessed.tempids,
        })
    }

    /// Normalize already expanded persistent transaction forms against one
    /// exact db-before. Entity-map attribute resolution consults only the
    /// immutable schema/ident caches; primitive forms pass through unchanged.
    /// Process-local callbacks deliberately remain on `Database::with_forms`.
    pub(crate) fn normalize_persisted_forms_with_limit(
        &self,
        forms: &[TxForm],
        max_primitive_ops: usize,
    ) -> Result<Vec<TxOp>, SemanticError> {
        normalize_forms_against(NormalizerRead::Exact(self), forms, None, max_primitive_ops)
    }
}

/// Explicit per-attempt resource policy, independent of transaction semantics.
/// The defaults match the durable service's operation, read and program limits.
#[derive(Clone, Copy, Debug)]
pub struct SpeculationLimits {
    pub max_operations: usize,
    pub max_read_datoms: u64,
    pub max_read_bytes: u64,
    pub program: crate::ProgramLimits,
    /// Distinct immutable programs resolved by this attempt, including the
    /// complete fixed dependency closure of newly referenced code.
    pub max_program_dependencies: usize,
    /// Canonical payload bytes plus 1 KiB/key overhead per resolved program.
    /// The cumulative speculative database may retain code from earlier calls.
    pub max_program_bytes: usize,
}

impl Default for SpeculationLimits {
    fn default() -> Self {
        let durable = crate::CapacityLimits::default();
        Self {
            max_operations: durable.max_transaction_ops,
            max_read_datoms: durable.max_transaction_read_datoms,
            max_read_bytes: durable.max_transaction_read_bytes,
            program: durable.program,
            max_program_dependencies: 1_024,
            max_program_bytes: 64 * 1024 * 1024,
        }
    }
}

#[derive(Clone, Copy)]
enum NormalizerRead<'a> {
    Eager(&'a Database),
    Exact(&'a DatabaseValue),
}

impl<'a> NormalizerRead<'a> {
    fn schema(self) -> &'a crate::Schema {
        match self {
            Self::Eager(database) => database.schema(),
            Self::Exact(database) => database.schema(),
        }
    }

    fn entid(self, ident: &Keyword) -> Option<u64> {
        match self {
            Self::Eager(database) => database.entid(ident),
            Self::Exact(database) => database.entid(ident),
        }
    }

    fn eager(self) -> Option<&'a Database> {
        match self {
            Self::Eager(database) => Some(database),
            Self::Exact(_) => None,
        }
    }
}

fn normalize_forms_against(
    db_before: NormalizerRead<'_>,
    forms: &[TxForm],
    functions: Option<&TxFunctions>,
    max_primitive_ops: usize,
) -> Result<Vec<TxOp>, SemanticError> {
    validate_forms_input(forms)?;
    let mut forms = forms.to_vec();
    forms.sort_by(compare_tx_form);
    // A local callback may return many forms (and more callbacks). Preserve
    // incremental primitive admission while discovering that complete input.
    // Only this finite-budget, local-callback path needs a preflight; persisted
    // programs enforce their expansion budget before reaching this normalizer.
    let mut admission = (max_primitive_ops != usize::MAX
        && forms.iter().any(|form| matches!(form, TxForm::Call(_))))
    .then(|| {
        (
            Normalizer {
                db_before,
                explicit_tempids: BTreeSet::new(),
                next_anonymous: 0,
                primitive_count: 0,
                max_primitive_ops,
            },
            Vec::new(),
        )
    });
    let mut expanded = Vec::new();
    expand_local_calls(
        db_before,
        functions,
        forms,
        0,
        &mut expanded,
        &mut admission,
    )?;
    let mut normalizer = Normalizer {
        db_before,
        explicit_tempids: explicit_tempids(&expanded),
        next_anonymous: 0,
        primitive_count: 0,
        max_primitive_ops,
    };
    let mut ops = Vec::new();
    for form in expanded {
        normalizer.expand_form(&form, &mut ops)?;
    }
    Ok(ops)
}

/// Discover the complete author-supplied identity namespace before allocating
/// anonymous maps. A callback can emit an explicit tempid after an earlier
/// anonymous map, including through another callback. Every call still sees
/// the same db-before and executes exactly once. Keep the previous canonical
/// depth-first form order so noncolliding requests retain their allocations.
/// Persisted calls have already expanded at the authoritative boundary.
fn expand_local_calls(
    db_before: NormalizerRead<'_>,
    functions: Option<&TxFunctions>,
    forms: Vec<TxForm>,
    depth: usize,
    output: &mut Vec<TxForm>,
    admission: &mut Option<(Normalizer<'_>, Vec<TxOp>)>,
) -> Result<(), SemanticError> {
    for form in forms {
        if depth > 32 {
            return Err(SemanticError::incorrect(
                "transaction/function-depth",
                "transaction function expansion exceeded 32 nested calls",
            ));
        }
        match form {
            TxForm::Call(call) => {
                let (Some(database), Some(functions)) = (db_before.eager(), functions) else {
                    return Err(SemanticError::incorrect(
                        "transaction/process-local-function-requires-eager-db",
                        "process-local Rust transaction callbacks require the eager speculative Database API",
                    ));
                };
                let mut generated = functions.invoke(database, &call)?;
                validate_forms_input(&generated)?;
                generated.sort_by(compare_tx_form);
                expand_local_calls(
                    db_before,
                    Some(functions),
                    generated,
                    depth + 1,
                    output,
                    admission,
                )?;
            }
            TxForm::ProgramCall(_) => {
                return Err(SemanticError::incorrect(
                    "transaction/unresolved-database-function",
                    "persisted database-function calls must be resolved against db-before by the transactor",
                ));
            }
            form => {
                if let Some((normalizer, scratch)) = admission {
                    normalizer.expand_form(&form, scratch)?;
                    scratch.clear();
                }
                output.push(form);
            }
        }
    }
    Ok(())
}

fn explicit_tempids(forms: &[TxForm]) -> BTreeSet<String> {
    fn entity(reference: &EntityRef, names: &mut BTreeSet<String>) {
        match reference {
            EntityRef::Temp(name) => {
                names.insert(name.clone());
            }
            EntityRef::LookupInput { value: input, .. } => value(input, names),
            _ => {}
        }
    }
    fn value(input: &TxValue, names: &mut BTreeSet<String>) {
        match input {
            TxValue::Entity(reference) => entity(reference, names),
            TxValue::Tuple(slots) => {
                for slot in slots.iter().flatten() {
                    value(slot, names);
                }
            }
            TxValue::Scalar(_) => {}
        }
    }
    fn map(input: &EntityMap, names: &mut BTreeSet<String>) {
        if let Some(reference) = &input.id {
            entity(reference, names);
        }
        for (_, input) in &input.attributes {
            map_value(input, names);
        }
    }
    fn map_value(input: &MapValue, names: &mut BTreeSet<String>) {
        match input {
            MapValue::Value(input) => value(input, names),
            MapValue::Nested(input) => map(input, names),
            MapValue::Many(inputs) => {
                for input in inputs {
                    map_value(input, names);
                }
            }
        }
    }
    let mut names = BTreeSet::new();
    for form in forms {
        match form {
            TxForm::EntityMap(input) => map(input, &mut names),
            TxForm::Op(op) => match op {
                TxOp::Add {
                    entity: id,
                    value: input,
                    ..
                } => {
                    entity(id, &mut names);
                    value(input, &mut names);
                }
                TxOp::Retract {
                    entity: id,
                    value: input,
                    ..
                } => {
                    entity(id, &mut names);
                    if let Some(input) = input {
                        value(input, &mut names);
                    }
                }
                TxOp::Cas {
                    entity: id,
                    old,
                    new,
                    ..
                } => {
                    entity(id, &mut names);
                    if let Some(old) = old {
                        value(old, &mut names);
                    }
                    value(new, &mut names);
                }
                TxOp::RetractEntity(id) => entity(id, &mut names),
                TxOp::Ensure { entity: id, spec } => {
                    entity(id, &mut names);
                    entity(spec, &mut names);
                }
                TxOp::ForcePartition { tempid, partition } => {
                    names.insert(tempid.clone());
                    entity(partition, &mut names);
                }
                TxOp::MatchPartition { tempid, entity: id } => {
                    names.insert(tempid.clone());
                    entity(id, &mut names);
                }
                TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => {}
            },
            TxForm::Call(_) | TxForm::ProgramCall(_) => {
                unreachable!("all calls were expanded before anonymous allocation")
            }
        }
    }
    names
}

struct Normalizer<'a> {
    db_before: NormalizerRead<'a>,
    explicit_tempids: BTreeSet<String>,
    next_anonymous: u64,
    primitive_count: usize,
    max_primitive_ops: usize,
}

impl Normalizer<'_> {
    fn expand_form(&mut self, form: &TxForm, output: &mut Vec<TxOp>) -> Result<(), SemanticError> {
        match form {
            TxForm::Op(op) => self.push(op.clone(), output),
            TxForm::EntityMap(map) => {
                self.expand_map(map, None, 0, output)?;
                Ok(())
            }
            TxForm::Call(_) | TxForm::ProgramCall(_) => {
                unreachable!("all calls were expanded before anonymous allocation")
            }
        }
    }

    fn anonymous_tempid(&mut self) -> Result<EntityRef, SemanticError> {
        // Retain the existing deterministic receipt names where they do not
        // collide. Submitted forms (not these normalized names) determine the
        // request digest; already-committed retries return their stored receipt
        // before reaching normalization, including historical collisions.
        loop {
            let name = format!("__map/{:020}", self.next_anonymous);
            self.next_anonymous = self.next_anonymous.checked_add(1).ok_or_else(|| {
                SemanticError::new(
                    crate::ErrorCategory::Busy,
                    "transaction/anonymous-id-overflow",
                    "anonymous transaction identities exhausted their allocation range",
                )
            })?;
            if !self.explicit_tempids.contains(&name) {
                return Ok(EntityRef::Temp(name));
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
        let entity = match forced_id.or_else(|| map.id.clone()) {
            Some(entity) => entity,
            None => self.anonymous_tempid()?,
        };
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
                    // Installation maps naturally name the database partition
                    // with a keyword, including maps emitted by stored programs.
                    // This does not install vocabulary absent from db-before.
                    let source = match value {
                        TxValue::Entity(source) => source.clone(),
                        TxValue::Scalar(Value::Keyword(ident))
                            if u64::from(attribute) == crate::DB_INSTALL_PARTITION =>
                        {
                            EntityRef::Ident(ident.clone())
                        }
                        TxValue::Scalar(Value::Ref(id))
                            if u64::from(attribute) == crate::DB_INSTALL_PARTITION =>
                        {
                            EntityRef::Id(*id)
                        }
                        _ => {
                            return Err(SemanticError::incorrect(
                                "transaction/reverse-value-must-be-entity",
                                "a reverse attribute value must identify an entity",
                            ));
                        }
                    };
                    self.push(
                        TxOp::Add {
                            entity: source,
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
                    // The source's map expander gives nested component maps
                    // affinity with their owner. Ordinary primitive ref edges
                    // deliberately do not acquire this authoring policy.
                    if component {
                        if let EntityRef::Temp(tempid) = &child {
                            self.push(
                                TxOp::MatchPartition {
                                    tempid: tempid.clone(),
                                    entity: owner.clone(),
                                },
                                output,
                            )?;
                        }
                    }
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
                let installation_reverse = matches!(attribute, AttributeRef::Ident(_))
                    && ident == &Keyword::new("db.install", "_partition");
                let normalized = if installation_reverse {
                    Keyword::new("db.install", "partition")
                } else {
                    ident.clone()
                };
                let entity = self.db_before.entid(&normalized).ok_or_else(|| {
                    SemanticError::incorrect(
                        "schema/unknown-attribute",
                        format!("unknown attribute {}", ident.qualified_name()),
                    )
                })?;
                let id = crate::schema_eid_to_attr_id(entity)?;
                self.db_before.schema().attribute(id)?;
                Ok((
                    id,
                    installation_reverse || matches!(attribute, AttributeRef::ReverseIdent(_)),
                ))
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use bigdecimal::BigDecimal;
    use std::str::FromStr;

    const KEY: u32 = 1_000;
    const CHILD: u32 = 1_001;

    fn scalar(value: Value) -> MapValue {
        MapValue::Value(TxValue::Scalar(value))
    }

    #[test]
    fn callback_discovery_preserves_cumulative_primitive_admission() {
        use std::sync::atomic::{AtomicUsize, Ordering};
        let database = Database::bootstrap().unwrap();
        let invoked = Arc::new(AtomicUsize::new(0));
        let mut functions = TxFunctions::new();
        let count = Arc::clone(&invoked);
        functions.register("emit", move |_, _| {
            count.fetch_add(1, Ordering::Relaxed);
            Ok(vec![TxForm::Op(TxOp::Add {
                entity: EntityRef::Temp("some-entity".into()),
                attribute: crate::DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("test", "entity")).into(),
            })])
        });
        functions.register("branch", |_, _| {
            Ok((0..100)
                .map(|_| {
                    TxForm::Call(TxCall {
                        function: "emit".into(),
                        arguments: Vec::new(),
                    })
                })
                .collect())
        });
        let error = database
            .normalize_forms_with_limit(
                &[TxForm::Call(TxCall {
                    function: "branch".into(),
                    arguments: Vec::new(),
                })],
                &functions,
                2,
            )
            .unwrap_err();
        assert_eq!(error.code, "postgres/transaction-op-capacity");
        assert_eq!(
            invoked.load(Ordering::Relaxed),
            3,
            "stop at the first over-budget primitive; do not invoke the remaining 97 callbacks"
        );
    }

    #[test]
    fn nested_component_maps_emit_partition_affinity_but_primitive_edges_do_not() {
        let mut schema = crate::Schema::new();
        schema
            .install(Attribute::new(
                KEY,
                Keyword::new("person", "name"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        schema
            .install(
                Attribute::new(
                    CHILD,
                    Keyword::new("person", "child"),
                    ValueType::Ref,
                    Cardinality::One,
                )
                .component(),
            )
            .unwrap();
        let db = Database::new(schema).unwrap();
        let forms = [TxForm::EntityMap(EntityMap {
            id: Some(EntityRef::Temp("parent".into())),
            attributes: vec![(
                AttributeRef::Id(CHILD),
                MapValue::Nested(Box::new(EntityMap {
                    id: Some(EntityRef::Temp("child".into())),
                    attributes: vec![(
                        AttributeRef::Id(KEY),
                        scalar(Value::String("child".into())),
                    )],
                })),
            )],
        })];
        let normalized = db.normalize_forms(&forms, &TxFunctions::new()).unwrap();
        assert!(normalized.iter().any(|op| matches!(op, TxOp::MatchPartition { tempid, entity: EntityRef::Temp(parent) } if tempid == "child" && parent == "parent")));
        let exact = db
            .database_value()
            .normalize_persisted_forms_with_limit(&forms, 16)
            .unwrap();
        assert_eq!(normalized.len(), exact.len());
        assert!(
            normalized
                .iter()
                .zip(&exact)
                .all(|(a, b)| compare_tx_op(a, b) == Ordering::Equal)
        );
        let primitives = [TxForm::Op(TxOp::Add {
            entity: EntityRef::Temp("parent".into()),
            attribute: CHILD,
            value: TxValue::Entity(EntityRef::Temp("child".into())),
        })];
        assert_eq!(
            db.normalize_forms(&primitives, &TxFunctions::new())
                .unwrap()
                .len(),
            1
        );
    }

    #[test]
    fn partition_install_maps_lower_to_ordinary_installation_facts() {
        let db = Database::bootstrap().unwrap();
        for attribute in [
            AttributeRef::ReverseId(crate::DB_INSTALL_PARTITION as u32),
            AttributeRef::ReverseIdent(Keyword::new("db.install", "partition")),
            AttributeRef::Ident(Keyword::new("db.install", "_partition")),
        ] {
            let forms = [TxForm::EntityMap(EntityMap {
                id: Some(EntityRef::Temp("partition".into())),
                attributes: vec![
                    (
                        AttributeRef::Id(crate::DB_IDENT as u32),
                        scalar(Value::Keyword(Keyword::new("part", "customers"))),
                    ),
                    (
                        attribute,
                        scalar(Value::Keyword(Keyword::new("db.part", "db"))),
                    ),
                ],
            })];
            let ops = db.normalize_forms(&forms, &TxFunctions::new()).unwrap();
            assert_eq!(ops.len(), 2);
            assert!(ops.iter().any(|op| matches!(op, TxOp::Add { entity: EntityRef::Ident(ident), attribute, value: TxValue::Entity(EntityRef::Temp(tempid)) } if ident == &Keyword::new("db.part", "db") && u64::from(*attribute) == crate::DB_INSTALL_PARTITION && tempid == "partition")));
            assert!(!ops.iter().any(|op| matches!(
                op,
                TxOp::ForcePartition { .. } | TxOp::MatchPartition { .. }
            )));
            let exact = db
                .database_value()
                .normalize_persisted_forms_with_limit(&forms, 16)
                .unwrap();
            assert!(
                ops.iter()
                    .zip(&exact)
                    .all(|(a, b)| compare_tx_op(a, b) == Ordering::Equal)
            );
        }
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

    #[test]
    fn exact_database_value_normalizes_entity_maps_like_the_eager_oracle() {
        let mut schema = crate::Schema::new();
        schema
            .install(
                Attribute::new(
                    KEY,
                    Keyword::new("person", "key"),
                    ValueType::String,
                    Cardinality::One,
                )
                .unique(Unique::Identity),
            )
            .unwrap();
        schema
            .install(Attribute::new(
                CHILD,
                Keyword::new("person", "child"),
                ValueType::Ref,
                Cardinality::One,
            ))
            .unwrap();
        let database = Database::new(schema).unwrap();
        let forms = vec![TxForm::EntityMap(EntityMap {
            id: Some(EntityRef::Temp("parent".into())),
            attributes: vec![(
                AttributeRef::Ident(Keyword::new("person", "child")),
                MapValue::Nested(Box::new(EntityMap {
                    id: None,
                    attributes: vec![(
                        AttributeRef::Ident(Keyword::new("person", "key")),
                        scalar(Value::String("child-key".into())),
                    )],
                })),
            )],
        })];

        let eager = database
            .normalize_forms_with_limit(&forms, &TxFunctions::new(), 16)
            .unwrap();
        let exact = database
            .database_value()
            .normalize_persisted_forms_with_limit(&forms, 16)
            .unwrap();
        assert_eq!(eager.len(), exact.len());
        assert!(
            eager
                .iter()
                .zip(&exact)
                .all(|(eager, exact)| compare_tx_op(eager, exact) == Ordering::Equal)
        );
    }

    #[test]
    fn exact_normalizer_rejects_process_local_callbacks() {
        let database = Database::bootstrap().unwrap().database_value();
        let error = database
            .normalize_persisted_forms_with_limit(
                &[TxForm::Call(TxCall {
                    function: "local/only".into(),
                    arguments: Vec::new(),
                })],
                16,
            )
            .unwrap_err();
        assert_eq!(
            error.code,
            "transaction/process-local-function-requires-eager-db"
        );
    }

    #[test]
    fn persisted_entity_predicate_accepts_eager_and_exact_database_values() {
        let database = Database::bootstrap().unwrap();
        let mut functions = TxFunctions::new();
        functions.register_entity_value_predicate("test/exists", |database, entity| {
            Ok(RuntimeValue::Scalar(Value::Bool(
                !database.values(entity, crate::DB_IDENT as u32)?.is_empty(),
            )))
        });
        let system_entity = crate::DB_IDENT;

        assert_eq!(
            functions
                .validate_entity_predicate("test/exists", &database, system_entity)
                .unwrap(),
            RuntimeValue::Scalar(Value::Bool(true))
        );
        assert_eq!(
            functions
                .validate_entity_predicate_exact(
                    "test/exists",
                    &database.database_value(),
                    system_entity,
                )
                .unwrap(),
            RuntimeValue::Scalar(Value::Bool(true))
        );
    }
}
