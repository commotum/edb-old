use crate::{
    Cardinality, Database, DatabaseValue, Datom, ErrorCategory, IndexBoundary, IndexComponents,
    IndexPrefix, Keyword, QueryValue, SemanticError, Symbol, Value, ValueType,
    schema_eid_to_attr_id,
};
use std::collections::{BTreeMap, BTreeSet};
use std::sync::{
    Arc, Mutex,
    atomic::{AtomicBool, Ordering},
};
use std::time::Instant;

#[cfg(test)]
#[path = "pull_bounded_tests.rs"]
mod bounded_tests;

#[derive(Clone, Debug, Eq, Ord, PartialEq, PartialOrd)]
pub enum AttributeName {
    Id(u32),
    Ident(Keyword),
}

/// A read-side entity identifier resolved against one exact immutable
/// database value. Lookup refs deliberately carry an attribute name rather
/// than only an integer so historical aliases remain usable at the API edge.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum EntityIdentifier {
    Id(u64),
    Ident(Keyword),
    Lookup {
        attribute: AttributeName,
        value: Value,
    },
}

impl From<u64> for EntityIdentifier {
    fn from(value: u64) -> Self {
        Self::Id(value)
    }
}

impl From<Keyword> for EntityIdentifier {
    fn from(value: Keyword) -> Self {
        Self::Ident(value)
    }
}

impl From<&Keyword> for EntityIdentifier {
    fn from(value: &Keyword) -> Self {
        Self::Ident(value.clone())
    }
}

#[derive(Clone, Debug, Eq, Ord, PartialEq, PartialOrd)]
pub enum PullDirection {
    Forward(AttributeName),
    Reverse(AttributeName),
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub enum PullLimit {
    #[default]
    Default,
    Limit(usize),
    Unlimited,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum PullNested {
    Pattern(Box<PullPattern>),
    Recursion(Option<usize>),
}

type TransformFunction = dyn Fn(&QueryValue) -> Result<QueryValue, SemanticError> + Send + Sync;

/// Peer-local value transformation, applied after limits/nesting and before a
/// default. A missing attribute is passed as `QueryValue::Nil`. Native callbacks
/// are explicitly supplied Rust code, not dynamically resolved JVM functions.
/// Callbacks are cooperatively controlled: cancellation/deadlines are checked
/// before and after invocation, but cannot preempt arbitrary Rust code.
#[derive(Clone)]
pub enum PullTransform {
    /// Native readable text. Strings are unquoted at the top level; collections
    /// and maps include delimiters. This is not a JVM/wire serialization format.
    String,
    Keyword,
    Symbol,
    Name,
    Namespace,
    Function {
        name: Arc<str>,
        function: Arc<TransformFunction>,
    },
}

impl PullTransform {
    pub fn new(
        name: impl Into<Arc<str>>,
        function: impl Fn(&QueryValue) -> Result<QueryValue, SemanticError> + Send + Sync + 'static,
    ) -> Self {
        Self::Function {
            name: name.into(),
            function: Arc::new(function),
        }
    }

    fn apply(
        &self,
        value: &QueryValue,
        state: &mut PullState<'_, '_>,
    ) -> Result<QueryValue, SemanticError> {
        let scalar = |value| Ok(QueryValue::Scalar(value));
        match (self, value) {
            (Self::Function { function, .. }, value) => function(value),
            (Self::String, value) => scalar(Value::String(pull_value_text(value, state)?)),
            (Self::Keyword, QueryValue::Nil) => Ok(QueryValue::Nil),
            (Self::Keyword, QueryValue::Scalar(Value::Keyword(value))) => {
                scalar(Value::Keyword(value.clone()))
            }
            (Self::Keyword, QueryValue::Scalar(Value::Symbol(value))) => {
                scalar(Value::Keyword(Keyword {
                    namespace: value.namespace.clone(),
                    name: value.name.clone(),
                }))
            }
            (Self::Keyword, QueryValue::Scalar(Value::String(value))) => {
                let (namespace, name) = pull_name_parts(value.strip_prefix(':').unwrap_or(value));
                scalar(Value::Keyword(Keyword { namespace, name }))
            }
            (Self::Symbol, QueryValue::Scalar(Value::Symbol(value))) => {
                scalar(Value::Symbol(value.clone()))
            }
            (Self::Symbol, QueryValue::Scalar(Value::String(value))) => {
                let (namespace, name) = pull_name_parts(value);
                scalar(Value::Symbol(Symbol { namespace, name }))
            }
            (Self::Name, QueryValue::Scalar(Value::String(value))) => {
                scalar(Value::String(value.clone()))
            }
            (Self::Name, QueryValue::Scalar(Value::Keyword(value))) => {
                scalar(Value::String(value.name.clone()))
            }
            (Self::Name, QueryValue::Scalar(Value::Symbol(value))) => {
                scalar(Value::String(value.name.clone()))
            }
            (Self::Namespace, QueryValue::Scalar(Value::Keyword(value))) => {
                Ok(value.namespace.as_ref().map_or(QueryValue::Nil, |value| {
                    QueryValue::Scalar(Value::String(value.clone()))
                }))
            }
            (Self::Namespace, QueryValue::Scalar(Value::Symbol(value))) => {
                Ok(value.namespace.as_ref().map_or(QueryValue::Nil, |value| {
                    QueryValue::Scalar(Value::String(value.clone()))
                }))
            }
            _ => Err(SemanticError::incorrect(
                "pull/transform-type",
                "pull transform does not accept this value type",
            )),
        }
    }
}

impl std::fmt::Debug for PullTransform {
    fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::String => formatter.write_str("String"),
            Self::Keyword => formatter.write_str("Keyword"),
            Self::Symbol => formatter.write_str("Symbol"),
            Self::Name => formatter.write_str("Name"),
            Self::Namespace => formatter.write_str("Namespace"),
            Self::Function { name, .. } => formatter.debug_tuple("Function").field(name).finish(),
        }
    }
}

impl PartialEq for PullTransform {
    fn eq(&self, other: &Self) -> bool {
        match (self, other) {
            (
                Self::Function { function: left, .. },
                Self::Function {
                    function: right, ..
                },
            ) => Arc::ptr_eq(left, right),
            _ => std::mem::discriminant(self) == std::mem::discriminant(other),
        }
    }
}

impl Eq for PullTransform {}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct PullAttribute {
    pub direction: PullDirection,
    pub alias: Option<QueryValue>,
    pub default: Option<QueryValue>,
    pub limit: PullLimit,
    pub nested: Option<PullNested>,
    pub transform: Option<PullTransform>,
}

impl PullAttribute {
    pub fn forward(attribute: AttributeName) -> Self {
        Self {
            direction: PullDirection::Forward(attribute),
            alias: None,
            default: None,
            limit: PullLimit::Default,
            nested: None,
            transform: None,
        }
    }

    pub fn reverse(attribute: AttributeName) -> Self {
        Self {
            direction: PullDirection::Reverse(attribute),
            alias: None,
            default: None,
            limit: PullLimit::Default,
            nested: None,
            transform: None,
        }
    }
}

#[derive(Debug, Default)]
pub struct PullPattern {
    pub wildcard: bool,
    pub attributes: Vec<PullAttribute>,
}

impl Clone for PullPattern {
    fn clone(&self) -> Self {
        enum Task<'a> {
            Visit(&'a PullPattern),
            Finish(&'a PullPattern, usize),
        }
        let mut pending = vec![Task::Visit(self)];
        let mut results = Vec::new();
        while let Some(task) = pending.pop() {
            match task {
                Task::Visit(pattern) => {
                    let children = pattern.attributes.iter().filter_map(|attribute| {
                        if let Some(PullNested::Pattern(child)) = &attribute.nested {
                            Some(child.as_ref())
                        } else {
                            None
                        }
                    });
                    pending.push(Task::Finish(pattern, children.clone().count()));
                    pending.extend(children.rev().map(Task::Visit));
                }
                Task::Finish(pattern, count) => {
                    let mut children = results.split_off(results.len() - count).into_iter();
                    let attributes = pattern
                        .attributes
                        .iter()
                        .map(|attribute| PullAttribute {
                            direction: attribute.direction.clone(),
                            alias: attribute.alias.clone(),
                            default: attribute.default.clone(),
                            limit: attribute.limit,
                            transform: attribute.transform.clone(),
                            nested: match &attribute.nested {
                                Some(PullNested::Pattern(_)) => Some(PullNested::Pattern(
                                    Box::new(children.next().expect("nested pattern result")),
                                )),
                                Some(PullNested::Recursion(limit)) => {
                                    Some(PullNested::Recursion(*limit))
                                }
                                None => None,
                            },
                        })
                        .collect();
                    results.push(Self {
                        wildcard: pattern.wildcard,
                        attributes,
                    });
                }
            }
        }
        results.pop().expect("one pattern clone")
    }
}

impl Drop for PullPattern {
    fn drop(&mut self) {
        let mut pending = std::mem::take(&mut self.attributes);
        while let Some(mut attribute) = pending.pop() {
            if let Some(PullNested::Pattern(mut pattern)) = attribute.nested.take() {
                pending.append(&mut pattern.attributes);
            }
        }
    }
}

impl PartialEq for PullPattern {
    fn eq(&self, other: &Self) -> bool {
        let mut pending = vec![(self, other)];
        while let Some((left, right)) = pending.pop() {
            if left.wildcard != right.wildcard || left.attributes.len() != right.attributes.len() {
                return false;
            }
            for (left, right) in left.attributes.iter().zip(&right.attributes) {
                if left.direction != right.direction
                    || left.alias != right.alias
                    || left.default != right.default
                    || left.limit != right.limit
                    || left.transform != right.transform
                {
                    return false;
                }
                match (&left.nested, &right.nested) {
                    (Some(PullNested::Pattern(left)), Some(PullNested::Pattern(right))) => {
                        pending.push((left, right));
                    }
                    (Some(PullNested::Recursion(left)), Some(PullNested::Recursion(right)))
                        if left == right => {}
                    (None, None) => {}
                    _ => return false,
                }
            }
        }
        true
    }
}

impl Eq for PullPattern {}

#[derive(Clone, Debug)]
pub struct PullControl {
    /// Explicit traversal depth policy, with the root at zero. The default
    /// imposes no practical cap; selector recursion limits remain independent.
    pub max_depth: usize,
    /// Explicit number of expanded entity occurrences, counting repeated
    /// branches independently. The default imposes no practical cap.
    pub max_entities: usize,
    pub cancel: Arc<AtomicBool>,
}

impl Default for PullControl {
    fn default() -> Self {
        Self {
            max_depth: usize::MAX,
            max_entities: usize::MAX,
            cancel: Arc::new(AtomicBool::new(false)),
        }
    }
}

struct PullState<'a, 'cancel> {
    control: &'a PullControl,
    query_budget: Option<&'a mut QueryPullBudget<'cancel>>,
    /// Cycle and depth state belongs to one lexical recursive selector. An
    /// ordinary nested pull, or a different recursive selector in the same
    /// pattern, must not consume this state.
    recursions: BTreeMap<RecursionKey, RecursionState>,
    entities: usize,
}

/// Shared execution budget used when pull is a query result transformation.
///
/// Datomic's query timeout covers pull work too. Keeping this state separate
/// from the public `PullControl` lets every pull expression in one query share
/// the already-consumed query work and the original absolute deadline.
pub(crate) struct QueryPullBudget<'a> {
    cancel: Arc<AtomicBool>,
    deadline: Option<Instant>,
    max_work: usize,
    work: usize,
    borrowed_cancel: Option<&'a AtomicBool>,
    value_bytes: usize,
    max_value_bytes: usize,
}

impl<'a> QueryPullBudget<'a> {
    pub(crate) fn new(
        cancel: Arc<AtomicBool>,
        deadline: Option<Instant>,
        max_work: usize,
        work: usize,
    ) -> Self {
        Self {
            cancel,
            deadline,
            max_work,
            work,
            borrowed_cancel: None,
            value_bytes: 0,
            max_value_bytes: usize::MAX,
        }
    }

    pub(crate) fn with_borrowed_cancel(mut self, cancel: Option<&'a AtomicBool>) -> Self {
        self.borrowed_cancel = cancel;
        self
    }

    pub(crate) fn with_value_budget(mut self, used: usize, max: usize) -> Self {
        self.value_bytes = used;
        self.max_value_bytes = max;
        self
    }

    pub(crate) fn value_bytes(&self) -> usize {
        self.value_bytes
    }

    pub(crate) fn remaining_value_bytes(&self) -> usize {
        self.max_value_bytes.saturating_sub(self.value_bytes)
    }

    pub(crate) fn charge_value_bytes(&mut self, bytes: usize) -> Result<(), SemanticError> {
        self.value_bytes = self.value_bytes.saturating_add(bytes);
        if self.value_bytes > self.max_value_bytes {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "query/value-byte-limit",
                "query projection exceeded its shared value allocation allowance",
            ));
        }
        Ok(())
    }

    pub(crate) fn work(&self) -> usize {
        self.work
    }

    pub(crate) fn check(&mut self, amount: usize) -> Result<(), SemanticError> {
        self.work = self.work.saturating_add(amount);
        if self.cancel.load(Ordering::Relaxed)
            || self
                .borrowed_cancel
                .is_some_and(|cancel| cancel.load(Ordering::Relaxed))
        {
            return Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "query/canceled",
                "query was canceled",
            ));
        }
        if self
            .deadline
            .is_some_and(|deadline| Instant::now() >= deadline)
        {
            return Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "query/timeout",
                "query deadline elapsed",
            ));
        }
        if self.work > self.max_work {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "query/work-limit",
                "query exceeded its work limit",
            ));
        }
        Ok(())
    }
}

impl PullState<'_, '_> {
    fn read_candidate(&mut self, datom: Option<&Datom>) -> Result<bool, SemanticError> {
        // Polls are traversal work too: an overlay may skip many removed base
        // facts before yielding a candidate. Charge those advances, not only
        // datoms that happen to survive its merge/window.
        self.check(1)?;
        if let Some(datom) = datom {
            // Cursor items are owned. Charge their actual inline/heap payload
            // before view filtering and before retaining values or tasks. A
            // filtered-out candidate still consumed work and an owned item.
            self.charge_value_bytes(usize::try_from(datom.retained_bytes()).unwrap_or(usize::MAX))?;
        }
        Ok(true)
    }

    fn charge_value_bytes(&mut self, bytes: usize) -> Result<(), SemanticError> {
        if let Some(budget) = self.query_budget.as_deref_mut() {
            budget.charge_value_bytes(bytes)?;
        }
        Ok(())
    }
    fn check(&mut self, amount: usize) -> Result<(), SemanticError> {
        if let Some(budget) = self.query_budget.as_deref_mut() {
            return budget.check(amount);
        }
        if self.control.cancel.load(Ordering::Relaxed) {
            return Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "pull/canceled",
                "pull was canceled",
            ));
        }
        Ok(())
    }
}

#[derive(Debug, Default)]
struct RecursionState {
    depth: usize,
    seen: BTreeSet<u64>,
}

#[derive(Clone, Debug, Eq, Ord, PartialEq, PartialOrd)]
enum RecursionKey {
    Selector(Vec<usize>),
    Component(Vec<usize>),
}

impl PullPattern {
    pub fn attributes(attributes: Vec<PullAttribute>) -> Self {
        Self {
            wildcard: false,
            attributes,
        }
    }

    pub fn wildcard() -> Self {
        Self {
            wildcard: true,
            attributes: Vec::new(),
        }
    }
}

#[derive(Clone, Debug)]
pub struct Entity {
    database: DatabaseValue,
    id: u64,
    cache: Option<Arc<Mutex<EntityCache>>>,
}

// Reference identity deliberately excludes the lazy cache and database basis.
// Attribute comparisons belong to Pull/query results, not entity handles.
impl PartialEq for Entity {
    fn eq(&self, other: &Self) -> bool {
        self.id == other.id && self.database.entity_origin == other.database.entity_origin
    }
}

impl Eq for Entity {}

impl std::hash::Hash for Entity {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        std::hash::Hash::hash(&self.database.entity_origin, state);
        std::hash::Hash::hash(&self.id, state);
    }
}

type EntityCache = BTreeMap<PullDirection, Option<EntityValue>>;

// A touched entity owns a potentially deep tree of cached component entities.
// Empty a uniquely owned cache before its Arc is dropped so normal destruction
// has the same stack-safety as navigation. Shared caches are drained by their
// final owner, without mutating another live entity's cache.
impl Drop for Entity {
    fn drop(&mut self) {
        fn drain(entity: &mut Entity, pending: &mut Vec<EntityValue>) {
            if let Some(cache) = entity.cache.take().and_then(Arc::into_inner) {
                let cache = cache
                    .into_inner()
                    .unwrap_or_else(|error| error.into_inner());
                pending.extend(cache.into_values().flatten());
            }
        }
        let mut pending = Vec::new();
        drain(self, &mut pending);
        while let Some(value) = pending.pop() {
            match value {
                EntityValue::Entity(mut entity) => drain(&mut entity, &mut pending),
                EntityValue::Collection(mut values) => pending.append(&mut values),
                EntityValue::Scalar(_) => {}
            }
        }
    }
}

/// One value reached through Datomic-style associative entity navigation.
/// Reference values remain lazy entities pinned to the same immutable
/// database; cardinality-many and non-component reverse navigation retain
/// collection shape even when only one value is present.
#[derive(Clone, Debug)]
pub enum EntityValue {
    Scalar(Value),
    Entity(Entity),
    Collection(Vec<EntityValue>),
}

impl Entity {
    /// Compatibility constructor for the eager semantic oracle. Exact-value
    /// callers should use [`Entity::from_database_value`].
    pub fn new(database: Arc<Database>, id: u64) -> Self {
        Self::from_resolved(DatabaseValue::from(database), id)
    }

    pub fn from_database_value(database: DatabaseValue, id: u64) -> Result<Self, SemanticError> {
        database.require_point_in_time("entity")?;
        Ok(Self::from_resolved(database, id))
    }

    fn from_resolved(database: DatabaseValue, id: u64) -> Self {
        Self {
            database,
            id,
            cache: Some(Arc::new(Mutex::new(BTreeMap::new()))),
        }
    }

    pub fn id(&self) -> u64 {
        self.id
    }

    /// A cheap identity token suitable for map/set keys. No attributes are read.
    /// Use a snapshot key as well when the key must distinguish time views.
    pub fn identity(&self) -> crate::EntityIdentity {
        crate::EntityIdentity {
            origin: self.database.entity_origin.clone(),
            entity: self.id,
        }
    }

    pub fn database(&self) -> DatabaseValue {
        self.database.clone()
    }

    pub fn get(&self, attribute: u32) -> Result<Option<EntityValue>, SemanticError> {
        self.get_direction(&PullDirection::Forward(AttributeName::Id(attribute)))
    }

    pub fn get_named(
        &self,
        attribute: &AttributeName,
    ) -> Result<Option<EntityValue>, SemanticError> {
        self.get_direction(&PullDirection::Forward(attribute.clone()))
    }

    /// Associative forward or reverse navigation. Unknown keyword attributes
    /// behave like missing map keys; malformed numeric attributes remain an
    /// API error.
    pub fn get_direction(
        &self,
        direction: &PullDirection,
    ) -> Result<Option<EntityValue>, SemanticError> {
        if let Some(value) = self
            .cache
            .as_ref()
            .expect("live entity cache")
            .lock()
            .expect("entity cache poisoned")
            .get(direction)
        {
            return Ok(value.clone());
        }
        let value = self.read_direction(direction)?;
        self.cache
            .as_ref()
            .expect("live entity cache")
            .lock()
            .expect("entity cache poisoned")
            .insert(direction.clone(), value.clone());
        Ok(value)
    }

    fn read_direction(
        &self,
        direction: &PullDirection,
    ) -> Result<Option<EntityValue>, SemanticError> {
        self.database.require_point_in_time("entity")?;
        let (name, reverse) = match direction {
            PullDirection::Forward(name) => (name, false),
            PullDirection::Reverse(name) => (name, true),
        };
        if !reverse && is_db_id(name) {
            return Ok(Some(EntityValue::Scalar(Value::Ref(self.id))));
        }
        let Some(attribute) = resolve_pull_attribute(&self.database, name)? else {
            return Ok(None);
        };
        let schema = self.database.schema().attribute(attribute)?;
        if reverse && schema.value_type != ValueType::Ref {
            return Err(SemanticError::incorrect(
                "pull/reverse-non-ref",
                "reverse navigation requires a ref attribute",
            ));
        }
        let mut values = if reverse {
            self.database
                .datoms_with_prefix(&IndexPrefix::Vaet {
                    value: Value::Ref(self.id),
                    attribute: Some(attribute),
                    entity: None,
                })?
                .iter()
                .map(|datom| Value::Ref(datom.entity))
                .collect::<Vec<_>>()
        } else {
            self.database.values(self.id, attribute)?
        };
        if values.is_empty() {
            return Ok(None);
        }
        let multiple = if reverse {
            !schema.component
        } else {
            schema.cardinality == Cardinality::Many
        };
        if multiple {
            // EAVT groups equal forward values and VAET groups equal reverse
            // entities contiguously.  A filtered value can expose more than
            // one assertion event for the same logical E/A/V, but associative
            // entity navigation remains set-valued.
            values.dedup();
        }
        let mut values = values
            .into_iter()
            .map(|value| entity_navigation_value(&self.database, schema.value_type, value))
            .collect::<Result<Vec<_>, _>>()?;
        Ok(Some(if multiple {
            EntityValue::Collection(values)
        } else {
            values.remove(0)
        }))
    }

    pub fn reverse(&self, attribute: u32) -> Result<Vec<Entity>, SemanticError> {
        let value = self.get_direction(&PullDirection::Reverse(AttributeName::Id(attribute)))?;
        Ok(match value {
            None => Vec::new(),
            Some(EntityValue::Entity(entity)) => vec![entity],
            Some(EntityValue::Collection(values)) => values
                .into_iter()
                .map(|value| match value {
                    EntityValue::Entity(entity) => Ok(entity),
                    _ => Err(fault(
                        "entity/reverse-shape",
                        "reverse ref navigation did not produce entities",
                    )),
                })
                .collect::<Result<Vec<_>, _>>()?,
            Some(EntityValue::Scalar(_)) => {
                return Err(fault(
                    "entity/reverse-shape",
                    "reverse ref navigation did not produce entities",
                ));
            }
        })
    }

    /// Return available forward keys without realizing their values.
    pub fn keys(&self) -> Result<BTreeSet<Keyword>, SemanticError> {
        let mut keys = BTreeSet::from([db_id()]);
        for datom in self.database.datoms_with_prefix(&IndexPrefix::Eavt {
            entity: self.id,
            attribute: None,
            value: None,
        })? {
            keys.insert(
                self.database
                    .schema()
                    .attribute(datom.attribute)?
                    .ident
                    .clone(),
            );
        }
        Ok(keys)
    }

    /// Realize every direct attribute and recursively realize component
    /// entities. Non-component references remain lazy. Cyclic component paths
    /// stop at the revisited entity; traversal and cache destruction are iterative.
    pub fn touch(&self) -> Result<Self, SemanticError> {
        enum Task {
            Entity(Entity),
            Value(EntityValue),
            Leave(u64),
        }
        let mut pending = vec![Task::Entity(self.clone())];
        let mut path = BTreeSet::new();
        while let Some(task) = pending.pop() {
            match task {
                Task::Entity(entity) => {
                    if !path.insert(entity.id) {
                        continue;
                    }
                    pending.push(Task::Leave(entity.id));
                    let attributes = entity
                        .database
                        .datoms_with_prefix(&IndexPrefix::Eavt {
                            entity: entity.id,
                            attribute: None,
                            value: None,
                        })?
                        .iter()
                        .map(|datom| datom.attribute)
                        .collect::<BTreeSet<_>>();
                    for attribute in attributes {
                        let schema = entity.database.schema().attribute(attribute)?;
                        let value = entity.get(attribute)?;
                        if schema.component
                            && let Some(value) = value
                        {
                            pending.push(Task::Value(value));
                        }
                    }
                }
                Task::Value(EntityValue::Entity(entity)) => pending.push(Task::Entity(entity)),
                Task::Value(EntityValue::Collection(values)) => {
                    pending.extend(values.into_iter().rev().map(Task::Value));
                }
                Task::Value(EntityValue::Scalar(_)) => {}
                Task::Leave(entity) => {
                    path.remove(&entity);
                }
            }
        }
        Ok(self.clone())
    }

    pub fn pull(&self, pattern: &PullPattern) -> Result<QueryValue, SemanticError> {
        self.database.pull(pattern, self.id)
    }
}

fn entity_navigation_value(
    database: &DatabaseValue,
    value_type: ValueType,
    value: Value,
) -> Result<EntityValue, SemanticError> {
    if value_type != ValueType::Ref {
        return Ok(EntityValue::Scalar(value));
    }
    let Value::Ref(entity) = value else {
        return Err(fault(
            "entity/schema-value-mismatch",
            "ref attribute contains a non-ref value",
        ));
    };
    if let Some(ident) = database.ident(entity) {
        Ok(EntityValue::Scalar(Value::Keyword(ident.clone())))
    } else {
        Ok(EntityValue::Entity(Entity::from_resolved(
            database.clone(),
            entity,
        )))
    }
}

impl Database {
    /// Eager-oracle compatibility wrapper over one exact database value.
    pub fn resolve_entity_identifier(
        &self,
        identifier: &EntityIdentifier,
    ) -> Result<Option<u64>, SemanticError> {
        self.database_value().resolve_entity_identifier(identifier)
    }

    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        self.database_value().entity(identifier)
    }

    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        self.database_value().pull(pattern, entity)
    }

    pub fn pull_with_control(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
        control: &PullControl,
    ) -> Result<QueryValue, SemanticError> {
        self.database_value()
            .pull_with_control(pattern, entity, control)
    }

    pub fn pull_many<I, E>(
        &self,
        pattern: &PullPattern,
        entities: I,
    ) -> Result<Vec<QueryValue>, SemanticError>
    where
        I: IntoIterator<Item = E>,
        E: Into<EntityIdentifier>,
    {
        self.database_value().pull_many(pattern, entities)
    }
}

impl DatabaseValue {
    /// Construct a lazy associative entity over this exact immutable value.
    /// A history value spans time and therefore cannot back an entity.
    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        self.require_point_in_time("entity")?;
        Ok(self
            .resolve_entity_identifier(&identifier.into())?
            .map(|entity| Entity::from_resolved(self.clone(), entity)))
    }

    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        self.pull_with_control(pattern, entity, &PullControl::default())
    }

    pub fn pull_with_control(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
        control: &PullControl,
    ) -> Result<QueryValue, SemanticError> {
        validate_pattern(self, pattern)?;
        self.require_point_in_time("pull")?;
        let mut state = PullState {
            control,
            query_budget: None,
            recursions: BTreeMap::new(),
            entities: 0,
        };
        state.check(0)?;
        let Some(entity) = self
            .resolve_entity_identifier_with_control(&entity.into(), &mut |datom| {
                state.read_candidate(datom)
            })?
        else {
            return Ok(unresolved_pull(pattern));
        };
        state.check(0)?;
        pull_entity(self, entity, pattern, &[], &mut state, 0)
    }

    /// Evaluate a find-pull expression under the enclosing query's shared
    /// cancellation, deadline, and work budget.
    pub(crate) fn pull_for_query(
        &self,
        pattern: &PullPattern,
        entity: u64,
        budget: &mut QueryPullBudget,
    ) -> Result<QueryValue, SemanticError> {
        validate_pattern(self, pattern)?;
        self.require_point_in_time("pull")?;
        let control = PullControl {
            cancel: Arc::clone(&budget.cancel),
            ..PullControl::default()
        };
        let mut state = PullState {
            control: &control,
            query_budget: Some(budget),
            recursions: BTreeMap::new(),
            entities: 0,
        };
        pull_entity(self, entity, pattern, &[], &mut state, 0)
    }

    pub fn pull_many<I, E>(
        &self,
        pattern: &PullPattern,
        entities: I,
    ) -> Result<Vec<QueryValue>, SemanticError>
    where
        I: IntoIterator<Item = E>,
        E: Into<EntityIdentifier>,
    {
        // Pattern compilation/validation precedes entity traversal in the
        // recovered implementation, including for an empty input collection.
        validate_pattern(self, pattern)?;
        self.require_point_in_time("pull")?;
        entities
            .into_iter()
            .map(|entity| self.pull(pattern, entity))
            .collect()
    }
}

fn unresolved_pull(pattern: &PullPattern) -> QueryValue {
    let mut result = Vec::new();
    if pattern.wildcard {
        put(&mut result, keyword_key(db_id()), QueryValue::Nil);
    }
    for selector in &pattern.attributes {
        let PullDirection::Forward(name) = &selector.direction else {
            continue;
        };
        if is_db_id(name) {
            let key = selector
                .alias
                .clone()
                .unwrap_or_else(|| keyword_key(db_id()));
            put(&mut result, key, QueryValue::Nil);
        }
    }
    QueryValue::Map(result)
}

#[derive(Clone)]
struct PullContext<'a> {
    pattern: &'a PullPattern,
    scope: Vec<usize>,
    depth: usize,
}

#[derive(Clone, Copy)]
enum Selector<'a> {
    Explicit(usize, &'a PullAttribute),
    Wildcard(u32),
}

enum PullTask<'a> {
    Entity(u64, PullContext<'a>),
    Attribute(u64, Selector<'a>, PullContext<'a>),
    Value {
        source: u64,
        value: Value,
        value_type: ValueType,
        component: bool,
        nested: Option<&'a PullNested>,
        selector_index: Option<usize>,
        context: PullContext<'a>,
    },
    FinishMap {
        start: usize,
        entries: Vec<(QueryValue, QueryValue)>,
    },
    FinishAttribute {
        start: usize,
        key: QueryValue,
        default: Option<&'a QueryValue>,
        transform: Option<&'a PullTransform>,
        multiple: bool,
        omit_empty: bool,
    },
    LeaveRecursion {
        key: RecursionKey,
        entity: u64,
        new_traversal: bool,
    },
}

enum PullOutput {
    Value(QueryValue),
    Attribute(Option<(QueryValue, QueryValue)>),
}

enum PreparedAttribute {
    Complete(QueryValue, Option<QueryValue>),
    Values {
        key: QueryValue,
        values: Vec<Value>,
        multiple: bool,
        value_type: ValueType,
        component: bool,
    },
}

/// Explicit continuations keep native stack depth independent of graph depth.
/// A recursive selector's visited set is removed at its lexical return point,
/// so siblings and unrelated nested patterns do not share accidental state.
fn pull_entity(
    database: &DatabaseValue,
    entity: u64,
    pattern: &PullPattern,
    pattern_scope: &[usize],
    state: &mut PullState<'_, '_>,
    depth: usize,
) -> Result<QueryValue, SemanticError> {
    let wildcard = PullPattern::wildcard();
    let mut tasks = vec![PullTask::Entity(
        entity,
        PullContext {
            pattern,
            scope: pattern_scope.to_vec(),
            depth,
        },
    )];
    let mut output = Vec::new();
    while let Some(task) = tasks.pop() {
        match task {
            PullTask::Entity(entity, context) => {
                state.check(1)?;
                state.charge_value_bytes(
                    std::mem::size_of::<PullTask>() + std::mem::size_of::<QueryValue>() * 3,
                )?;
                if context.depth > state.control.max_depth {
                    return Err(SemanticError::new(
                        ErrorCategory::Busy,
                        "pull/depth-limit",
                        "pull exceeded its recursion depth limit",
                    ));
                }
                state.entities = state.entities.checked_add(1).ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::Busy,
                        "pull/entity-limit",
                        "pull entity count overflowed",
                    )
                })?;
                if state.entities > state.control.max_entities {
                    return Err(SemanticError::new(
                        ErrorCategory::Busy,
                        "pull/entity-limit",
                        "pull exceeded its entity traversal limit",
                    ));
                }
                let mut entries = Vec::new();
                let mut selectors = Vec::new();
                let mut wildcard_processed = BTreeSet::new();
                if context.pattern.wildcard {
                    put(
                        &mut entries,
                        keyword_key(db_id()),
                        QueryValue::Scalar(Value::Ref(entity)),
                    );
                    // Recovered `a-iter`/`next-a` seeks to the next attribute,
                    // not the next datom. A many-valued attribute's unselected
                    // tail must not be scanned just to discover its neighbor.
                    let mut after = None;
                    while let Some(attribute) = next_pull_attribute(database, entity, after, state)?
                    {
                        after = Some(attribute);
                        let explicit =
                            context
                                .pattern
                                .attributes
                                .iter()
                                .enumerate()
                                .find(|(_, selector)| {
                                    selector_selects_forward(database, selector, attribute)
                                });
                        state.charge_value_bytes(std::mem::size_of::<Selector>())?;
                        selectors.push(match explicit {
                            Some((index, selector)) => {
                                wildcard_processed.insert(index);
                                Selector::Explicit(index, selector)
                            }
                            None => Selector::Wildcard(attribute),
                        });
                    }
                }
                for (index, selector) in context.pattern.attributes.iter().enumerate() {
                    if !wildcard_processed.contains(&index) {
                        selectors.push(Selector::Explicit(index, selector));
                    }
                }
                tasks.push(PullTask::FinishMap {
                    start: output.len(),
                    entries,
                });
                for selector in selectors.into_iter().rev() {
                    tasks.push(PullTask::Attribute(entity, selector, context.clone()));
                }
            }
            PullTask::Attribute(entity, selector, context) => {
                let default_selector;
                let (selector_index, nested, default, transform, selector) = match selector {
                    Selector::Explicit(index, selector) => (
                        Some(index),
                        selector.nested.as_ref(),
                        selector.default.as_ref(),
                        selector.transform.as_ref(),
                        selector,
                    ),
                    Selector::Wildcard(attribute) => {
                        default_selector = PullAttribute::forward(AttributeName::Id(attribute));
                        (None, None, None, None, &default_selector)
                    }
                };
                match prepare_attribute(database, entity, selector, state)? {
                    PreparedAttribute::Complete(key, value) => output.push(PullOutput::Attribute(
                        finish_attribute(key, value, default, transform, state)?,
                    )),
                    PreparedAttribute::Values {
                        key,
                        values,
                        multiple,
                        value_type,
                        component,
                    } => {
                        tasks.push(PullTask::FinishAttribute {
                            start: output.len(),
                            key,
                            default,
                            transform,
                            multiple,
                            omit_empty: nested.is_some(),
                        });
                        for value in values.into_iter().rev() {
                            tasks.push(PullTask::Value {
                                source: entity,
                                value,
                                value_type,
                                component,
                                nested,
                                selector_index,
                                context: context.clone(),
                            });
                        }
                    }
                }
            }
            PullTask::Value {
                source,
                value,
                value_type,
                component,
                nested,
                selector_index,
                context,
            } => {
                let Value::Ref(entity) = value else {
                    if nested.is_some() {
                        return Err(SemanticError::incorrect(
                            "pull/nested-non-ref",
                            "nested pull requires a ref attribute",
                        ));
                    }
                    output.push(PullOutput::Value(QueryValue::Scalar(value)));
                    continue;
                };
                if value_type != ValueType::Ref {
                    return Err(fault(
                        "pull/schema-value-mismatch",
                        "ref value belongs to a non-ref attribute",
                    ));
                }
                let mut child_context = context.clone();
                child_context.depth = context.depth.checked_add(1).ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::Busy,
                        "pull/depth-limit",
                        "pull traversal depth overflowed",
                    )
                })?;
                let recursion = match nested {
                    Some(PullNested::Pattern(pattern)) => {
                        child_context.pattern = pattern;
                        child_context
                            .scope
                            .push(selector_index.expect("nested selector has an index"));
                        None
                    }
                    Some(PullNested::Recursion(limit)) => {
                        let mut path = context.scope.clone();
                        path.push(selector_index.expect("recursive selector has an index"));
                        Some((RecursionKey::Selector(path), *limit))
                    }
                    None if component => {
                        child_context.pattern = &wildcard;
                        Some((RecursionKey::Component(context.scope.clone()), None))
                    }
                    None => {
                        output.push(PullOutput::Value(id_map(entity)));
                        continue;
                    }
                };
                if let Some((key, limit)) = recursion {
                    let new_traversal = !state.recursions.contains_key(&key);
                    let recursion = state.recursions.entry(key.clone()).or_default();
                    if new_traversal {
                        recursion.seen.insert(source);
                    }
                    if limit.is_some_and(|limit| recursion.depth >= limit)
                        || recursion.seen.contains(&entity)
                    {
                        if new_traversal {
                            state.recursions.remove(&key);
                        }
                        output.push(PullOutput::Value(id_map(entity)));
                        continue;
                    }
                    recursion.depth += 1;
                    recursion.seen.insert(entity);
                    tasks.push(PullTask::LeaveRecursion {
                        key,
                        entity,
                        new_traversal,
                    });
                }
                tasks.push(PullTask::Entity(entity, child_context));
            }
            PullTask::FinishMap { start, mut entries } => {
                for item in output.drain(start..) {
                    let PullOutput::Attribute(attribute) = item else {
                        unreachable!("entity continuation requires attribute results")
                    };
                    if let Some((key, value)) = attribute {
                        put(&mut entries, key, value);
                    }
                }
                state.charge_value_bytes(
                    entries
                        .len()
                        .saturating_mul(std::mem::size_of::<(QueryValue, QueryValue)>()),
                )?;
                output.push(PullOutput::Value(QueryValue::Map(entries)));
            }
            PullTask::FinishAttribute {
                start,
                key,
                default,
                transform,
                multiple,
                omit_empty,
            } => {
                let mut values = Vec::new();
                for item in output.drain(start..) {
                    let PullOutput::Value(value) = item else {
                        unreachable!("attribute continuation requires value results")
                    };
                    if !omit_empty || !is_empty_map(&value) {
                        values.push(value);
                    }
                }
                state.charge_value_bytes(
                    values
                        .len()
                        .saturating_mul(std::mem::size_of::<QueryValue>()),
                )?;
                let value = if multiple {
                    Some(QueryValue::Collection(values))
                } else if values.is_empty() {
                    None
                } else {
                    Some(values.remove(0))
                };
                output.push(PullOutput::Attribute(finish_attribute(
                    key, value, default, transform, state,
                )?));
            }
            PullTask::LeaveRecursion {
                key,
                entity,
                new_traversal,
            } => {
                let recursion = state
                    .recursions
                    .get_mut(&key)
                    .expect("recursive selector remains active until its continuation");
                recursion.depth -= 1;
                recursion.seen.remove(&entity);
                if new_traversal {
                    state.recursions.remove(&key);
                }
            }
        }
    }
    match output.pop() {
        Some(PullOutput::Value(value)) => Ok(value),
        _ => unreachable!("root pull must return one value"),
    }
}

fn finish_attribute(
    key: QueryValue,
    value: Option<QueryValue>,
    default: Option<&QueryValue>,
    transform: Option<&PullTransform>,
    state: &mut PullState<'_, '_>,
) -> Result<Option<(QueryValue, QueryValue)>, SemanticError> {
    let value = if let Some(transform) = transform {
        state.check(1)?;
        let input = value.unwrap_or(QueryValue::Nil);
        let result = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
            transform.apply(&input, state)
        }))
        .map_err(|_| fault("pull/transform-panicked", "pull transform panicked"))?;
        state.check(0)?;
        let result = result?;
        state.charge_value_bytes(crate::query::query_value_allocation_bytes(&result))?;
        match result {
            QueryValue::Nil => None,
            value => Some(value),
        }
    } else {
        value
    };
    if value.is_none()
        && let Some(default) = default
    {
        state.charge_value_bytes(crate::query::query_value_allocation_bytes(default))?;
    }
    Ok(value.or_else(|| default.cloned()).map(|value| (key, value)))
}

fn pull_name_parts(value: &str) -> (Option<String>, String) {
    match value.split_once('/') {
        Some((namespace, name)) if !namespace.is_empty() && !name.is_empty() => {
            (Some(namespace.to_owned()), name.to_owned())
        }
        _ => (None, value.to_owned()),
    }
}

fn pull_value_text(
    value: &QueryValue,
    state: &mut PullState<'_, '_>,
) -> Result<String, SemanticError> {
    enum Task<'a> {
        Query(&'a QueryValue, bool),
        Stored(&'a Value, bool),
        Text(&'static str),
    }
    let mut pending = vec![Task::Query(value, false)];
    let mut result = String::new();
    while let Some(task) = pending.pop() {
        state.check(1)?;
        match task {
            Task::Text(text) => result.push_str(text),
            Task::Query(QueryValue::Nil, quoted) => {
                if quoted {
                    result.push_str("nil");
                }
            }
            Task::Query(QueryValue::Scalar(value), quoted) => {
                pending.push(Task::Stored(value, quoted));
            }
            Task::Query(QueryValue::Collection(values) | QueryValue::Tuple(values), _) => {
                result.push('[');
                pending.push(Task::Text("]"));
                for (index, value) in values.iter().enumerate().rev() {
                    pending.push(Task::Query(value, true));
                    if index > 0 {
                        pending.push(Task::Text(" "));
                    }
                }
            }
            Task::Query(QueryValue::Map(entries), _) => {
                result.push('{');
                pending.push(Task::Text("}"));
                for (index, (key, value)) in entries.iter().enumerate().rev() {
                    pending.push(Task::Query(value, true));
                    pending.push(Task::Text(" "));
                    pending.push(Task::Query(key, true));
                    if index > 0 {
                        pending.push(Task::Text(", "));
                    }
                }
            }
            Task::Stored(Value::Tuple(values), _) => {
                result.push('[');
                pending.push(Task::Text("]"));
                for (index, value) in values.iter().enumerate().rev() {
                    pending.push(match value {
                        Some(value) => Task::Stored(value, true),
                        None => Task::Text("nil"),
                    });
                    if index > 0 {
                        pending.push(Task::Text(" "));
                    }
                }
            }
            Task::Stored(value, quoted) => {
                let text = match value {
                    Value::String(value) if quoted => format!("{value:?}"),
                    Value::String(value) | Value::Uri(value) => value.clone(),
                    Value::Keyword(value) => format!(":{}", value.qualified_name()),
                    Value::Symbol(value) => value.qualified_name(),
                    Value::Long(value) | Value::Instant(value) => value.to_string(),
                    Value::Ref(value) => value.to_string(),
                    Value::Bool(value) => value.to_string(),
                    Value::Double(value) => value.to_string(),
                    Value::Float(value) => value.to_string(),
                    Value::BigDec(value) => value.to_string(),
                    Value::BigInt(value) => value.to_string(),
                    Value::Uuid(value) => format!("{value:032x}"),
                    Value::Bytes(value) => format!("#bytes{value:?}"),
                    Value::Function(value) => format!("#function{value:02x?}"),
                    Value::Tuple(_) => unreachable!("tuples use iterative tasks"),
                };
                result.push_str(&text);
            }
        }
    }
    Ok(result)
}

fn prepare_attribute(
    database: &DatabaseValue,
    entity: u64,
    selector: &PullAttribute,
    state: &mut PullState<'_, '_>,
) -> Result<PreparedAttribute, SemanticError> {
    state.check(1)?;
    let (name, reverse) = match &selector.direction {
        PullDirection::Forward(attribute) => (attribute, false),
        PullDirection::Reverse(attribute) => (attribute, true),
    };
    if !reverse && is_db_id(name) {
        let key = selector
            .alias
            .clone()
            .unwrap_or_else(|| keyword_key(db_id()));
        return Ok(PreparedAttribute::Complete(
            key,
            Some(QueryValue::Scalar(Value::Ref(entity))),
        ));
    }
    let Some(attribute) = resolve_pull_attribute(database, name)? else {
        let AttributeName::Ident(ident) = name else {
            unreachable!("numeric attributes are resolved strictly")
        };
        let key = selector
            .alias
            .clone()
            .unwrap_or_else(|| keyword_key(reverse_ident(ident, reverse)));
        return Ok(PreparedAttribute::Complete(key, None));
    };
    let schema = database.schema().attribute(attribute)?;
    if reverse && schema.value_type != ValueType::Ref {
        return Err(SemanticError::incorrect(
            "pull/reverse-non-ref",
            format!("{} is not a ref attribute", schema.ident.qualified_name()),
        ));
    }
    let key = selector.alias.clone().unwrap_or_else(|| {
        let ident = match name {
            AttributeName::Ident(ident) => ident,
            AttributeName::Id(_) => &schema.ident,
        };
        keyword_key(reverse_ident(ident, reverse))
    });
    state.charge_value_bytes(crate::query::query_value_allocation_bytes(&key))?;
    let multiple = if reverse {
        !schema.component
    } else {
        schema.cardinality == Cardinality::Many
    };
    let limit = if multiple {
        match selector.limit {
            PullLimit::Default => 1_000,
            PullLimit::Limit(limit) => limit,
            PullLimit::Unlimited => usize::MAX,
        }
    } else {
        1
    };
    let prefix = if reverse {
        IndexPrefix::Vaet {
            value: Value::Ref(entity),
            attribute: Some(attribute),
            entity: None,
        }
    } else {
        IndexPrefix::Eavt {
            entity,
            attribute: Some(attribute),
            value: None,
        }
    };
    let mut cursor = database.prefix_cursor(&prefix)?;
    let mut values = Vec::new();
    while values.len() < limit {
        let Some(datom) = cursor
            .next_with_control(&mut |datom| state.read_candidate(datom))
            .transpose()?
        else {
            break;
        };
        // A filter callback may have canceled while accepting the last value.
        state.check(0)?;
        state.charge_value_bytes(std::mem::size_of::<Value>())?;
        values.push(if reverse {
            Value::Ref(datom.entity)
        } else {
            datom.value
        });
    }
    if values.is_empty() {
        return Ok(PreparedAttribute::Complete(key, None));
    }
    Ok(PreparedAttribute::Values {
        key,
        values,
        multiple,
        value_type: schema.value_type,
        component: schema.component,
    })
}

fn next_pull_attribute(
    database: &DatabaseValue,
    entity: u64,
    after: Option<u32>,
    state: &mut PullState<'_, '_>,
) -> Result<Option<u32>, SemanticError> {
    state.check(0)?;
    let boundary = match after {
        None => IndexBoundary::Eavt(IndexComponents::One(entity)),
        Some(attribute) => match attribute.checked_add(1) {
            Some(next) => IndexBoundary::Eavt(IndexComponents::Two(entity, next)),
            None => return Ok(None),
        },
    };
    let mut cursor = database.seek_cursor(&boundary)?;
    let datom = cursor
        .next_with_control(&mut |datom| {
            state.read_candidate(datom)?;
            // Apply the entity fence before filtering so an empty remainder
            // cannot wander through other entities while looking for a match.
            Ok(datom.is_none_or(|datom| datom.entity == entity))
        })
        .transpose()?;
    state.check(0)?;
    Ok(datom.map(|datom| datom.attribute))
}

#[derive(Clone, Debug, Eq, Ord, PartialEq, PartialOrd)]
enum PullAttributeIdentity {
    DbId,
    Installed(u32),
    Unknown(Keyword),
}

fn validate_pattern(database: &DatabaseValue, pattern: &PullPattern) -> Result<(), SemanticError> {
    let mut pending = vec![pattern];
    while let Some(pattern) = pending.pop() {
        let mut selectors = BTreeSet::new();
        for selector in &pattern.attributes {
            if selector.limit == PullLimit::Limit(0) {
                return Err(SemanticError::incorrect(
                    "db.error/invalid-limit",
                    "pull limits must be positive or unlimited",
                ));
            }
            match selector.nested.as_ref() {
                Some(PullNested::Pattern(pattern)) => pending.push(pattern),
                Some(PullNested::Recursion(Some(0))) => {
                    return Err(SemanticError::incorrect(
                        "db.error/invalid-recur-limit",
                        "recursive pull limits must be positive",
                    ));
                }
                _ => {}
            }

            let (name, reverse) = match &selector.direction {
                PullDirection::Forward(name) => (name, false),
                PullDirection::Reverse(name) => (name, true),
            };
            let identity = if !reverse && is_db_id(name) {
                PullAttributeIdentity::DbId
            } else {
                match resolve_pull_attribute(database, name)? {
                    Some(attribute) => PullAttributeIdentity::Installed(attribute),
                    None => match name {
                        AttributeName::Ident(ident) => {
                            PullAttributeIdentity::Unknown(ident.clone())
                        }
                        AttributeName::Id(_) => unreachable!("numeric attributes resolve strictly"),
                    },
                }
            };
            if !selectors.insert((reverse, identity)) {
                return Err(SemanticError::incorrect(
                    "pull/duplicate-attribute",
                    "a pull pattern may specify an attribute only once per direction",
                ));
            }
        }
    }
    Ok(())
}

fn selector_selects_forward(
    database: &DatabaseValue,
    selector: &PullAttribute,
    attribute: u32,
) -> bool {
    let PullDirection::Forward(name) = &selector.direction else {
        return false;
    };
    resolve_pull_attribute(database, name).ok().flatten() == Some(attribute)
}

fn resolve_pull_attribute(
    database: &DatabaseValue,
    name: &AttributeName,
) -> Result<Option<u32>, SemanticError> {
    match name {
        AttributeName::Id(attribute) => {
            database.schema().attribute(*attribute)?;
            Ok(Some(*attribute))
        }
        AttributeName::Ident(ident) => {
            let Some(entity) = database.entid(ident) else {
                return Ok(None);
            };
            let Ok(attribute) = schema_eid_to_attr_id(entity) else {
                return Ok(None);
            };
            Ok(database
                .schema()
                .attribute(attribute)
                .ok()
                .map(|_| attribute))
        }
    }
}

fn is_db_id(name: &AttributeName) -> bool {
    matches!(name, AttributeName::Ident(ident) if *ident == db_id())
}

fn is_empty_map(value: &QueryValue) -> bool {
    matches!(value, QueryValue::Map(entries) if entries.is_empty())
}

fn reverse_ident(ident: &Keyword, reverse: bool) -> Keyword {
    if !reverse {
        return ident.clone();
    }
    Keyword {
        namespace: ident.namespace.clone(),
        name: format!("_{}", ident.name),
    }
}

fn put(entries: &mut Vec<(QueryValue, QueryValue)>, key: QueryValue, value: QueryValue) {
    QueryValue::put_map_entry(entries, key, value);
}

fn id_map(entity: u64) -> QueryValue {
    QueryValue::Map(vec![(
        keyword_key(db_id()),
        QueryValue::Scalar(Value::Ref(entity)),
    )])
}

fn keyword_key(keyword: Keyword) -> QueryValue {
    QueryValue::Scalar(Value::Keyword(keyword))
}

fn db_id() -> Keyword {
    Keyword::new("db", "id")
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
