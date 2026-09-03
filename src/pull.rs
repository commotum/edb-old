use crate::{
    Cardinality, Database, ErrorCategory, IndexPrefix, Keyword, QueryValue, SemanticError, Value,
    ValueType,
};
use std::collections::BTreeSet;
use std::sync::{
    Arc,
    atomic::{AtomicBool, Ordering},
};

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum AttributeName {
    Id(u32),
    Ident(Keyword),
}

#[derive(Clone, Debug, Eq, PartialEq)]
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
    pub alias: Option<Keyword>,
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
    visited: BTreeSet<u64>,
    entities: usize,
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
    database: Arc<Database>,
    id: u64,
}

impl Entity {
    pub fn new(database: Arc<Database>, id: u64) -> Self {
        Self { database, id }
    }

    pub fn id(&self) -> u64 {
        self.id
    }

    pub fn database(&self) -> Arc<Database> {
        Arc::clone(&self.database)
    }

    pub fn get(&self, attribute: u32) -> Result<Option<QueryValue>, SemanticError> {
        let schema = self.database.schema().attribute(attribute)?;
        let values = self.database.values(self.id, attribute);
        if values.is_empty() {
            return Ok(None);
        }
        if schema.cardinality == Cardinality::Many {
            Ok(Some(QueryValue::Collection(
                values
                    .into_iter()
                    .cloned()
                    .map(QueryValue::Scalar)
                    .collect(),
            )))
        } else {
            Ok(Some(QueryValue::Scalar(values[0].clone())))
        }
    }

    pub fn reverse(&self, attribute: u32) -> Result<Vec<Entity>, SemanticError> {
        let schema = self.database.schema().attribute(attribute)?;
        if schema.value_type != ValueType::Ref {
            return Err(SemanticError::incorrect(
                "pull/reverse-non-ref",
                "reverse navigation requires a ref attribute",
            ));
        }
        let datoms = self.database.datoms_with_prefix(&IndexPrefix::Vaet {
            value: Value::Ref(self.id),
            attribute: Some(attribute),
            entity: None,
        })?;
        Ok(datoms
            .iter()
            .map(|datom| Entity::new(Arc::clone(&self.database), datom.entity))
            .collect())
    }

    pub fn pull(&self, pattern: &PullPattern) -> Result<QueryValue, SemanticError> {
        self.database.pull(pattern, self.id)
    }
}

impl Database {
    pub fn pull(&self, pattern: &PullPattern, entity: u64) -> Result<QueryValue, SemanticError> {
        self.pull_with_control(pattern, entity, &PullControl::default())
    }

    pub fn pull_with_control(
        &self,
        pattern: &PullPattern,
        entity: u64,
        control: &PullControl,
    ) -> Result<QueryValue, SemanticError> {
        let mut state = PullState {
            control,
            visited: BTreeSet::new(),
            entities: 0,
        };
        pull_entity(self, entity, pattern, pattern, &mut state, 0)
    }

    pub fn pull_many(
        &self,
        pattern: &PullPattern,
        entities: impl IntoIterator<Item = u64>,
    ) -> Result<Vec<QueryValue>, SemanticError> {
        entities
            .into_iter()
            .map(|entity| self.pull(pattern, entity))
            .collect()
    }
}

fn pull_entity(
    database: &Database,
    entity: u64,
    pattern: &PullPattern,
    recursion_pattern: &PullPattern,
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
    if !state.visited.insert(entity) {
        return Ok(id_map(entity));
    }
    let mut result = Vec::new();
    if pattern.wildcard {
        put(&mut result, db_id(), QueryValue::Scalar(Value::Ref(entity)));
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
            let selector = PullAttribute::forward(AttributeName::Id(attribute));
            if let Some((key, value)) =
                pull_attribute(database, entity, &selector, recursion_pattern, state, depth)?
            {
                put(&mut result, key, value);
            }
        }
    }
    for selector in &pattern.attributes {
        if let Some((key, value)) =
            pull_attribute(database, entity, selector, recursion_pattern, state, depth)?
        {
            put(&mut result, key, value);
        }
    }
    state.visited.remove(&entity);
    Ok(QueryValue::Map(result))
}

#[allow(clippy::too_many_arguments)]
fn pull_attribute(
    database: &Database,
    entity: u64,
    selector: &PullAttribute,
    recursion_pattern: &PullPattern,
    state: &mut PullState<'_>,
    depth: usize,
) -> Result<Option<(Keyword, QueryValue)>, SemanticError> {
    let (attribute, reverse) = match &selector.direction {
        PullDirection::Forward(attribute) => (resolve_attribute(database, attribute)?, false),
        PullDirection::Reverse(attribute) => (resolve_attribute(database, attribute)?, true),
    };
    let schema = database.schema().attribute(attribute)?;
    if reverse && schema.value_type != ValueType::Ref {
        return Err(SemanticError::incorrect(
            "pull/reverse-non-ref",
            format!("{} is not a ref attribute", schema.ident.qualified_name()),
        ));
    }
    let key = selector
        .alias
        .clone()
        .unwrap_or_else(|| reverse_ident(&schema.ident, reverse));
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
        database
            .values(entity, attribute)
            .into_iter()
            .cloned()
            .collect()
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
        pulled.push(pull_value(
            database,
            value,
            schema.value_type,
            schema.component,
            selector.nested.as_ref(),
            recursion_pattern,
            state,
            depth,
        )?);
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
    database: &Database,
    value: Value,
    value_type: ValueType,
    component: bool,
    nested: Option<&PullNested>,
    recursion_pattern: &PullPattern,
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
            pull_entity(database, entity, pattern, pattern, state, depth + 1)
        }
        Some(PullNested::Recursion(Some(limit))) if depth >= *limit => Ok(id_map(entity)),
        Some(PullNested::Recursion(_)) => pull_entity(
            database,
            entity,
            recursion_pattern,
            recursion_pattern,
            state,
            depth + 1,
        ),
        None if component => {
            let pattern = PullPattern::wildcard();
            pull_entity(database, entity, &pattern, &pattern, state, depth + 1)
        }
        None => Ok(id_map(entity)),
    }
}

fn resolve_attribute(database: &Database, name: &AttributeName) -> Result<u32, SemanticError> {
    match name {
        AttributeName::Id(attribute) => {
            database.schema().attribute(*attribute)?;
            Ok(*attribute)
        }
        AttributeName::Ident(ident) => database.schema().resolve_ident(ident).ok_or_else(|| {
            SemanticError::incorrect(
                "pull/unknown-attribute",
                format!("unknown attribute {}", ident.qualified_name()),
            )
        }),
    }
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

fn put(entries: &mut Vec<(Keyword, QueryValue)>, key: Keyword, value: QueryValue) {
    if let Some((_, existing)) = entries.iter_mut().find(|(candidate, _)| *candidate == key) {
        *existing = value;
    } else {
        entries.push((key, value));
        entries.sort_by(|(left, _), (right, _)| left.cmp(right));
    }
}

fn id_map(entity: u64) -> QueryValue {
    QueryValue::Map(vec![(db_id(), QueryValue::Scalar(Value::Ref(entity)))])
}

fn db_id() -> Keyword {
    Keyword::new("db", "id")
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
