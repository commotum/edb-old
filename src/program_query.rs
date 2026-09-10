//! Version-2 persisted query authoring over the existing native query engine.
//! No evaluator or source authority lives in this module.
use super::{ProgramBudget, ProgramRead, RuntimeValue, incorrect, query_value};
use crate::{
    DatabaseValue, InputSpec, Query, QueryDataSource, QueryEngine, QueryInput, QueryResult,
    QueryValue, SemanticError, TimePoint, Value,
};

/// A literal documented time point, or an argument containing Long (T), Ref
/// (transaction entity), or Instant (Unix milliseconds).
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum QueryTemplateTime {
    Literal(TimePoint),
    Argument(u8),
}

/// One named view of the exact database supplied to the program invocation.
/// Bounds use ordinary native as-of/since semantics; history retains both
/// assertions and retractions. Existing filters remain attached to the value.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct QueryTemplateSource {
    pub name: String,
    pub history: bool,
    /// Full captured committed log, not a database history projection.
    pub log: bool,
    pub as_of: Option<QueryTemplateTime>,
    pub since: Option<QueryTemplateTime>,
}

impl QueryTemplateSource {
    pub fn current(name: impl Into<String>) -> Self {
        Self {
            name: name.into(),
            history: false,
            log: false,
            as_of: None,
            since: None,
        }
    }
    pub fn history(name: impl Into<String>) -> Self {
        Self {
            history: true,
            ..Self::current(name)
        }
    }
    pub fn log(name: impl Into<String>) -> Self {
        Self {
            log: true,
            ..Self::current(name)
        }
    }
    pub fn as_of(mut self, point: QueryTemplateTime) -> Self {
        self.as_of = Some(point);
        self
    }
    pub fn since(mut self, point: QueryTemplateTime) -> Self {
        self.since = Some(point);
        self
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct NativeQueryTemplate {
    pub(crate) query: Query,
    pub(crate) input_arguments: Vec<u8>,
    pub(crate) sources: Vec<QueryTemplateSource>,
}

impl NativeQueryTemplate {
    pub(crate) fn new(
        query: Query,
        input_arguments: Vec<u8>,
        mut sources: Vec<QueryTemplateSource>,
    ) -> Result<Self, SemanticError> {
        if sources.is_empty() {
            sources.push(QueryTemplateSource::current("$"));
        }
        sources.sort_by(|left, right| left.name.cmp(&right.name));
        let template = Self {
            query,
            input_arguments,
            sources,
        };
        template.validate()?;
        Ok(template)
    }

    pub(crate) fn validate(&self) -> Result<(), SemanticError> {
        if self.query.inputs.len() != self.input_arguments.len() {
            return Err(incorrect(
                "program/query-input-arity",
                "each native query input needs a program argument binding",
            ));
        }
        if self.sources.is_empty()
            || self.sources.len() > super::MAX_QUERY_PATTERNS
            || self
                .sources
                .iter()
                .any(|source| source.name.is_empty() || source.name.len() > 256)
            || self
                .sources
                .windows(2)
                .any(|sources| sources[0].name >= sources[1].name)
        {
            return Err(incorrect(
                "program/query-sources",
                "native query sources must have unique bounded names in canonical order",
            ));
        }
        if self.sources.iter().any(|source| {
            source.log && (source.history || source.as_of.is_some() || source.since.is_some())
        }) {
            return Err(incorrect(
                "program/query-log-view",
                "log sources use their captured committed basis; supply ranges to tx-ids rather than database view modifiers",
            ));
        }
        // This bounded codec validation rejects local callbacks and unstable
        // built-ins before such values enter an immutable durable artifact.
        crate::encoding::validate_native_query(&self.query)?;
        crate::query::validate_program_query(&self.query, self.input_arguments.len())?;
        self.validate_arity(super::MAX_ARITY)
    }

    pub(crate) fn validate_arity(&self, arity: u8) -> Result<(), SemanticError> {
        let bounds = self
            .sources
            .iter()
            .flat_map(|source| [&source.as_of, &source.since])
            .filter_map(|bound| match bound {
                Some(QueryTemplateTime::Argument(index)) => Some(*index),
                _ => None,
            });
        if self
            .input_arguments
            .iter()
            .copied()
            .chain(bounds)
            .any(|index| index >= arity)
        {
            return Err(incorrect(
                "program/query-input-index",
                "native query binding is outside the program arity",
            ));
        }
        Ok(())
    }
}

fn time_point(
    bound: &QueryTemplateTime,
    arguments: &[RuntimeValue],
) -> Result<TimePoint, SemanticError> {
    Ok(match bound {
        QueryTemplateTime::Literal(point) => *point,
        QueryTemplateTime::Argument(index) => match &arguments[usize::from(*index)] {
            RuntimeValue::Scalar(Value::Long(t)) if *t >= 0 => TimePoint::T(*t as u64),
            RuntimeValue::Scalar(Value::Ref(tx))
            | RuntimeValue::Entity(crate::EntityRef::Id(tx)) => TimePoint::Tx(*tx),
            RuntimeValue::Scalar(Value::Instant(instant)) => TimePoint::Instant(*instant),
            _ => {
                return Err(incorrect(
                    "program/query-time-point",
                    "query time argument must be a nonnegative T, transaction entity, or instant",
                ));
            }
        },
    })
}

fn input(spec: &InputSpec, argument: &RuntimeValue) -> Result<QueryInput, SemanticError> {
    let scalar = |value: &RuntimeValue| query_value(value.clone());
    let vector = |value: &RuntimeValue| match value {
        RuntimeValue::Vector(values) => values.iter().map(scalar).collect(),
        _ => Err(incorrect(
            "program/query-input-shape",
            "query collection/tuple input needs a runtime vector",
        )),
    };
    Ok(match spec {
        InputSpec::Scalar(_) => QueryInput::Scalar(scalar(argument)?),
        InputSpec::Tuple(_) => QueryInput::Tuple(vector(argument)?),
        InputSpec::Collection(_) => QueryInput::Collection(vector(argument)?),
        InputSpec::Relation(_) => match argument {
            RuntimeValue::Vector(rows) => {
                QueryInput::Relation(rows.iter().map(vector).collect::<Result<_, _>>()?)
            }
            _ => {
                return Err(incorrect(
                    "program/query-input-shape",
                    "query relation input needs a vector of row vectors",
                ));
            }
        },
    })
}

fn runtime_value(value: QueryValue, depth: usize) -> Result<RuntimeValue, SemanticError> {
    if depth > 16 {
        return Err(incorrect(
            "program/value-depth",
            "query result exceeds the runtime value nesting limit",
        ));
    }
    // QueryValue has a stack-safe Drop, so take containers through its public
    // moving accessors instead of destructuring their owned children.
    match &value {
        QueryValue::Nil => Ok(RuntimeValue::Null),
        QueryValue::Scalar(scalar) => Ok(RuntimeValue::Scalar(scalar.clone())),
        QueryValue::Tuple(_) => value
            .into_tuple()
            .unwrap()
            .into_iter()
            .map(|value| runtime_value(value, depth + 1))
            .collect::<Result<Vec<_>, _>>()
            .map(RuntimeValue::Vector),
        QueryValue::Collection(_) => value
            .into_collection()
            .unwrap()
            .into_iter()
            .map(|value| runtime_value(value, depth + 1))
            .collect::<Result<Vec<_>, _>>()
            .map(RuntimeValue::Vector),
        QueryValue::Map(_) => {
            let entries = value
                .into_map()
                .unwrap()
                .into_iter()
                .map(|(key, value)| {
                    let QueryValue::Scalar(key) = &key else {
                        return Err(incorrect(
                            "program/query-map-key",
                            "runtime map keys must be stored scalar values",
                        ));
                    };
                    Ok((key.clone(), runtime_value(value, depth + 1)?))
                })
                .collect::<Result<Vec<_>, _>>()?;
            RuntimeValue::map(entries)
        }
    }
}

pub(super) fn execute(
    database: ProgramRead<'_>,
    arguments: &[RuntimeValue],
    budget: &mut ProgramBudget<'_>,
    template: &NativeQueryTemplate,
) -> Result<RuntimeValue, SemanticError> {
    budget.check_cancel()?;
    let database: DatabaseValue = match database {
        ProgramRead::Exact(database) => database.clone(),
        ProgramRead::Eager(database) => database.database_value(),
        ProgramRead::AttributePredicate => {
            unreachable!("attribute predicates cannot read databases")
        }
    };
    let mut sources = Vec::with_capacity(template.sources.len());
    for source in &template.sources {
        budget.charge(1)?;
        if source.log {
            sources.push(QueryDataSource::log(
                source.name.clone(),
                database.log_value()?,
            ));
            continue;
        }
        let mut view = database.clone();
        if let Some(bound) = &source.as_of {
            view = view.as_of_time_point(time_point(bound, arguments)?)?;
        }
        if let Some(bound) = &source.since {
            view = view.since_time_point(time_point(bound, arguments)?)?;
        }
        if source.history {
            view = view.history();
        }
        sources.push(QueryDataSource::database(source.name.clone(), view));
    }
    let inputs = template
        .query
        .inputs
        .iter()
        .zip(&template.input_arguments)
        .map(|(spec, index)| input(spec, &arguments[usize::from(*index)]))
        .collect::<Result<Vec<_>, _>>()?;
    let outcome = QueryEngine::execute_program(&template.query, &sources, &inputs, budget)?;
    budget.check_cancel()?;
    match outcome.result {
        QueryResult::Relation(rows) => rows
            .into_iter()
            .map(|row| {
                row.into_iter()
                    .map(|value| runtime_value(value, 1))
                    .collect::<Result<Vec<_>, _>>()
                    .map(RuntimeValue::Vector)
            })
            .collect::<Result<Vec<_>, _>>()
            .map(RuntimeValue::Vector),
        QueryResult::Collection(values) => values
            .into_iter()
            .map(|value| runtime_value(value, 1))
            .collect::<Result<Vec<_>, _>>()
            .map(RuntimeValue::Vector),
        QueryResult::Tuple(Some(values)) => values
            .into_iter()
            .map(|value| runtime_value(value, 1))
            .collect::<Result<Vec<_>, _>>()
            .map(RuntimeValue::Vector),
        QueryResult::Scalar(Some(value)) => runtime_value(value, 0),
        QueryResult::Tuple(None) | QueryResult::Scalar(None) => Ok(RuntimeValue::Null),
    }
}
