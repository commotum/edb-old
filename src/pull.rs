use crate::{
    Cardinality, Database, DatabaseValue, ErrorCategory, IndexPrefix, Keyword, QueryValue,
    SemanticError, Value, ValueType, schema_eid_to_attr_id,
};
use std::collections::{BTreeMap, BTreeSet};
use std::sync::{
    Arc, Mutex,
    atomic::{AtomicBool, Ordering},
};

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

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct PullAttribute {
    pub direction: PullDirection,
    pub alias: Option<QueryValue>,
    pub default: Option<QueryValue>,
    pub limit: PullLimit,
    pub nested: Option<PullNested>,
}

impl PullAttribute {
    pub fn forward(attribute: AttributeName) -> Self {
        Self {
            direction: PullDirection::Forward(attribute),
            alias: None,
            default: None,
            limit: PullLimit::Default,
            nested: None,
        }
    }

    pub fn reverse(attribute: AttributeName) -> Self {
        Self {
            direction: PullDirection::Reverse(attribute),
            alias: None,
            default: None,
            limit: PullLimit::Default,
            nested: None,
        }
    }
}

#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct PullPattern {
    pub wildcard: bool,
    pub attributes: Vec<PullAttribute>,
}

#[derive(Clone, Debug)]
pub struct PullControl {
    pub max_depth: usize,
    pub max_entities: usize,
    pub cancel: Arc<AtomicBool>,
}

impl Default for PullControl {
    fn default() -> Self {
        Self {
            max_depth: 512,
            max_entities: 100_000,
            cancel: Arc::new(AtomicBool::new(false)),
        }
    }
}

struct PullState<'a> {
    control: &'a PullControl,
    /// Cycle and depth state belongs to one lexical recursive selector. An
    /// ordinary nested pull, or a different recursive selector in the same
    /// pattern, must not consume this state.
    recursions: BTreeMap<Vec<usize>, RecursionState>,
    entities: usize,
}

#[derive(Debug, Default)]
struct RecursionState {
    depth: usize,
    seen: BTreeSet<u64>,
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
    cache: Arc<Mutex<BTreeMap<PullDirection, Option<EntityValue>>>>,
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
            cache: Arc::new(Mutex::new(BTreeMap::new())),
        }
    }

    pub fn id(&self) -> u64 {
        self.id
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
            .lock()
            .expect("entity cache poisoned")
            .get(direction)
        {
            return Ok(value.clone());
        }
        let value = self.read_direction(direction)?;
        self.cache
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
        let values = if reverse {
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
    /// entities. Non-component references remain lazy.
    pub fn touch(&self) -> Result<Self, SemanticError> {
        let attributes = self
            .database
            .datoms_with_prefix(&IndexPrefix::Eavt {
                entity: self.id,
                attribute: None,
                value: None,
            })?
            .iter()
            .map(|datom| datom.attribute)
            .collect::<BTreeSet<_>>();
        for attribute in attributes {
            let schema = self.database.schema().attribute(attribute)?;
            let value = self.get(attribute)?;
            if schema.component
                && let Some(value) = value.as_ref()
            {
                touch_entity_value(value)?;
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

fn touch_entity_value(value: &EntityValue) -> Result<(), SemanticError> {
    match value {
        EntityValue::Entity(entity) => {
            entity.touch()?;
        }
        EntityValue::Collection(values) => {
            for value in values {
                touch_entity_value(value)?;
            }
        }
        EntityValue::Scalar(_) => {}
    }
    Ok(())
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
        let Some(entity) = self.resolve_entity_identifier(&entity.into())? else {
            return Ok(QueryValue::Map(Vec::new()));
        };
        let mut state = PullState {
            control,
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

fn pull_entity(
    database: &DatabaseValue,
    entity: u64,
    pattern: &PullPattern,
    pattern_scope: &[usize],
    state: &mut PullState<'_>,
    depth: usize,
) -> Result<QueryValue, SemanticError> {
    if state.control.cancel.load(Ordering::Relaxed) {
        return Err(SemanticError::new(
            ErrorCategory::Interrupted,
            "pull/canceled",
            "pull was canceled",
        ));
    }
    if depth > state.control.max_depth {
        return Err(SemanticError::new(
            ErrorCategory::Busy,
            "pull/depth-limit",
            "pull exceeded its recursion depth limit",
        ));
    }
    state.entities += 1;
    if state.entities > state.control.max_entities {
        return Err(SemanticError::new(
            ErrorCategory::Busy,
            "pull/entity-limit",
            "pull exceeded its entity traversal limit",
        ));
    }
    let mut result = Vec::new();
    let mut wildcard_processed = BTreeSet::new();
    if pattern.wildcard {
        put(
            &mut result,
            keyword_key(db_id()),
            QueryValue::Scalar(Value::Ref(entity)),
        );
        let datoms = database.datoms_with_prefix(&IndexPrefix::Eavt {
            entity,
            attribute: None,
            value: None,
        })?;
        let mut attributes = BTreeSet::new();
        for datom in datoms {
            attributes.insert(datom.attribute);
        }
        for attribute in attributes {
            let default_selector = PullAttribute::forward(AttributeName::Id(attribute));
            let explicit = pattern
                .attributes
                .iter()
                .enumerate()
                .find(|(_, selector)| selector_selects_forward(database, selector, attribute));
            let (selector_index, selector) = match explicit {
                Some((selector_index, selector)) => {
                    wildcard_processed.insert(selector_index);
                    (Some(selector_index), selector)
                }
                None => (None, &default_selector),
            };
            if let Some((key, value)) = pull_attribute(
                database,
                entity,
                selector,
                pattern,
                pattern_scope,
                selector_index,
                state,
                depth,
            )? {
                put(&mut result, key, value);
            }
        }
    }
    for (selector_index, selector) in pattern.attributes.iter().enumerate() {
        if wildcard_processed.contains(&selector_index) {
            continue;
        }
        if let Some((key, value)) = pull_attribute(
            database,
            entity,
            selector,
            pattern,
            pattern_scope,
            Some(selector_index),
            state,
            depth,
        )? {
            put(&mut result, key, value);
        }
    }
    Ok(QueryValue::Map(result))
}

#[allow(clippy::too_many_arguments)]
fn pull_attribute(
    database: &DatabaseValue,
    entity: u64,
    selector: &PullAttribute,
    pattern: &PullPattern,
    pattern_scope: &[usize],
    selector_index: Option<usize>,
    state: &mut PullState<'_>,
    depth: usize,
) -> Result<Option<(QueryValue, QueryValue)>, SemanticError> {
    let (name, reverse) = match &selector.direction {
        PullDirection::Forward(attribute) => (attribute, false),
        PullDirection::Reverse(attribute) => (attribute, true),
    };
    if !reverse && is_db_id(name) {
        let key = selector
            .alias
            .clone()
            .unwrap_or_else(|| keyword_key(db_id()));
        return Ok(Some((key, QueryValue::Scalar(Value::Ref(entity)))));
    }
    let Some(attribute) = resolve_pull_attribute(database, name)? else {
        let AttributeName::Ident(ident) = name else {
            unreachable!("numeric attributes are resolved strictly")
        };
        let key = selector
            .alias
            .clone()
            .unwrap_or_else(|| keyword_key(reverse_ident(ident, reverse)));
        return Ok(selector.default.clone().map(|default| (key, default)));
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
    let mut values: Vec<Value> = if reverse {
        database
            .datoms_with_prefix(&IndexPrefix::Vaet {
                value: Value::Ref(entity),
                attribute: Some(attribute),
                entity: None,
            })?
            .iter()
            .map(|datom| Value::Ref(datom.entity))
            .collect()
    } else {
        database.values(entity, attribute)?
    };
    if values.is_empty() {
        return Ok(selector.default.clone().map(|default| (key, default)));
    }
    let multiple = if reverse {
        !schema.component
    } else {
        schema.cardinality == Cardinality::Many
    };
    if multiple {
        let limit = match selector.limit {
            PullLimit::Default => Some(1_000),
            PullLimit::Limit(limit) => Some(limit),
            PullLimit::Unlimited => None,
        };
        if let Some(limit) = limit {
            values.truncate(limit);
        }
    } else {
        values.truncate(1);
    }
    let mut pulled = Vec::new();
    for value in values {
        let value = pull_value(
            database,
            entity,
            value,
            schema.value_type,
            schema.component,
            selector.nested.as_ref(),
            pattern,
            pattern_scope,
            selector_index,
            state,
            depth,
        )?;
        if selector.nested.is_none() || !is_empty_map(&value) {
            pulled.push(value);
        }
    }
    if pulled.is_empty() && !multiple {
        return Ok(selector.default.clone().map(|default| (key, default)));
    }
    Ok(Some((
        key,
        if multiple {
            QueryValue::Collection(pulled)
        } else {
            pulled.remove(0)
        },
    )))
}

#[allow(clippy::too_many_arguments)]
fn pull_value(
    database: &DatabaseValue,
    source_entity: u64,
    value: Value,
    value_type: ValueType,
    component: bool,
    nested: Option<&PullNested>,
    pattern: &PullPattern,
    pattern_scope: &[usize],
    selector_index: Option<usize>,
    state: &mut PullState<'_>,
    depth: usize,
) -> Result<QueryValue, SemanticError> {
    let Value::Ref(entity) = value else {
        if nested.is_some() {
            return Err(SemanticError::incorrect(
                "pull/nested-non-ref",
                "nested pull requires a ref attribute",
            ));
        }
        return Ok(QueryValue::Scalar(value));
    };
    if value_type != ValueType::Ref {
        return Err(fault(
            "pull/schema-value-mismatch",
            "ref value belongs to a non-ref attribute",
        ));
    }
    match nested {
        Some(PullNested::Pattern(pattern)) => {
            let mut nested_scope = pattern_scope.to_vec();
            nested_scope.push(selector_index.expect("nested selector must belong to its pattern"));
            pull_entity(database, entity, pattern, &nested_scope, state, depth + 1)
        }
        Some(PullNested::Recursion(limit)) => pull_recursive(
            database,
            source_entity,
            entity,
            pattern,
            pattern_scope,
            selector_index.expect("recursive selector must belong to its pattern"),
            *limit,
            state,
            depth,
        ),
        None if component => {
            let pattern = PullPattern::wildcard();
            pull_entity(database, entity, &pattern, pattern_scope, state, depth + 1)
        }
        None => Ok(id_map(entity)),
    }
}

#[allow(clippy::too_many_arguments)]
fn pull_recursive(
    database: &DatabaseValue,
    source_entity: u64,
    target_entity: u64,
    pattern: &PullPattern,
    pattern_scope: &[usize],
    selector_index: usize,
    limit: Option<usize>,
    state: &mut PullState<'_>,
    depth: usize,
) -> Result<QueryValue, SemanticError> {
    let mut selector_path = pattern_scope.to_vec();
    selector_path.push(selector_index);
    let new_traversal = !state.recursions.contains_key(&selector_path);
    let should_recurse = {
        let recursion = state.recursions.entry(selector_path.clone()).or_default();
        if new_traversal {
            recursion.seen.insert(source_entity);
        }
        let within_limit = limit.is_none_or(|limit| recursion.depth < limit);
        if within_limit && !recursion.seen.contains(&target_entity) {
            recursion.depth += 1;
            recursion.seen.insert(target_entity);
            true
        } else {
            false
        }
    };
    if !should_recurse {
        if new_traversal {
            state.recursions.remove(&selector_path);
        }
        return Ok(id_map(target_entity));
    }

    let result = pull_entity(
        database,
        target_entity,
        pattern,
        pattern_scope,
        state,
        depth + 1,
    );
    let recursion = state
        .recursions
        .get_mut(&selector_path)
        .expect("active recursive selector state must remain present");
    recursion.depth -= 1;
    recursion.seen.remove(&target_entity);
    if new_traversal {
        state.recursions.remove(&selector_path);
    }
    result
}

#[derive(Clone, Debug, Eq, Ord, PartialEq, PartialOrd)]
enum PullAttributeIdentity {
    DbId,
    Installed(u32),
    Unknown(Keyword),
}

fn validate_pattern(database: &DatabaseValue, pattern: &PullPattern) -> Result<(), SemanticError> {
    let mut selectors = BTreeSet::new();
    for selector in &pattern.attributes {
        if selector.limit == PullLimit::Limit(0) {
            return Err(SemanticError::incorrect(
                "db.error/invalid-limit",
                "pull limits must be positive or unlimited",
            ));
        }
        match selector.nested.as_ref() {
            Some(PullNested::Pattern(pattern)) => validate_pattern(database, pattern)?,
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
                    AttributeName::Ident(ident) => PullAttributeIdentity::Unknown(ident.clone()),
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
