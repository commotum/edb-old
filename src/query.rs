use crate::pull::QueryPullBudget;
use crate::{
    Database, DatabaseValue, ErrorCategory, IndexOrder, IndexPrefix, Program, ProgramControl,
    ProgramHash, ProgramKind, ProgramOutput, ProgramRuntime, PullPattern, SemanticError, Value,
    schema_eid_to_attr_id,
};
use std::collections::{BTreeMap, BTreeSet};
use std::fmt;
use std::panic::{AssertUnwindSafe, catch_unwind};
use std::sync::{
    Arc,
    atomic::{AtomicBool, Ordering as AtomicOrdering},
};
use std::time::{Duration, Instant};

#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub struct Variable(String);

impl Variable {
    pub fn new(name: impl Into<String>) -> Result<Self, SemanticError> {
        let name = name.into();
        if name.is_empty() {
            return Err(SemanticError::incorrect(
                "query/empty-variable",
                "query variable names cannot be empty",
            ));
        }
        Ok(Self(name))
    }

    pub fn name(&self) -> &str {
        &self.0
    }
}

impl From<&str> for Variable {
    fn from(value: &str) -> Self {
        Self(value.to_owned())
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum Term {
    Variable(Variable),
    Constant(Value),
    /// Query-language nil. Nil is a legal literal and binding value, but is
    /// deliberately not part of the persisted `Value` domain.
    Nil,
    Blank,
}

impl Term {
    pub fn var(name: impl Into<Variable>) -> Self {
        Self::Variable(name.into())
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct DataPattern {
    pub source: String,
    pub entity: Term,
    pub attribute: Term,
    pub value: Term,
    pub transaction: Option<Term>,
    pub added: Option<Term>,
}

impl DataPattern {
    pub fn new(entity: Term, attribute: Term, value: Term) -> Self {
        Self {
            source: "$".into(),
            entity,
            attribute,
            value,
            transaction: None,
            added: None,
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Predicate {
    Eq,
    NotEq,
    Less,
    LessOrEqual,
    Greater,
    GreaterOrEqual,
    Missing,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum Function {
    Ground,
    Add,
    Subtract,
    Multiply,
    Divide,
    Tuple,
    Untuple,
    GetElse,
    GetSome,
    Extension(String),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum Binding {
    Scalar(Variable),
    Tuple(Vec<Option<Variable>>),
    Collection(Variable),
    Relation(Vec<Option<Variable>>),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum Clause {
    Pattern(Box<DataPattern>),
    Predicate {
        predicate: Predicate,
        source: String,
        args: Vec<Term>,
    },
    Function {
        function: Function,
        source: String,
        args: Vec<Term>,
        binding: Binding,
    },
    Not {
        join: Option<Vec<Variable>>,
        clauses: Vec<Clause>,
    },
    Or {
        join: Option<Vec<Variable>>,
        branches: Vec<Vec<Clause>>,
    },
    Rule {
        source: String,
        name: String,
        args: Vec<Term>,
    },
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Rule {
    pub name: String,
    pub head: Vec<Variable>,
    pub required: BTreeSet<usize>,
    pub clauses: Vec<Clause>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum InputSpec {
    Scalar(Variable),
    Tuple(Vec<Option<Variable>>),
    Collection(Variable),
    Relation(Vec<Option<Variable>>),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum QueryInput {
    Scalar(Value),
    Tuple(Vec<Value>),
    Collection(Vec<Value>),
    Relation(Vec<Vec<Value>>),
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum Aggregate {
    Count,
    CountDistinct,
    Min,
    Max,
    Sum,
    Average,
    Distinct,
    Median,
    Variance,
    StandardDeviation,
    MinN(usize),
    MaxN(usize),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum FindElement {
    Variable(Variable),
    Pull {
        source: String,
        variable: Variable,
        pattern: Box<PullPattern>,
    },
    Aggregate {
        function: Aggregate,
        variable: Variable,
    },
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum FindSpec {
    Relation(Vec<FindElement>),
    Collection(FindElement),
    Tuple(Vec<FindElement>),
    Scalar(FindElement),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Query {
    pub find: FindSpec,
    pub with: Vec<Variable>,
    pub inputs: Vec<InputSpec>,
    pub clauses: Vec<Clause>,
    pub rules: Vec<Rule>,
}

impl Query {
    pub fn new(find: FindSpec, clauses: Vec<Clause>) -> Self {
        Self {
            find,
            with: Vec::new(),
            inputs: Vec::new(),
            clauses,
            rules: Vec::new(),
        }
    }
}

#[derive(Clone, Debug)]
pub struct QuerySource {
    pub name: String,
    pub database: DatabaseValue,
}

#[derive(Clone, Debug)]
pub enum QueryValue {
    Nil,
    Scalar(Value),
    Collection(Vec<QueryValue>),
    Tuple(Vec<QueryValue>),
    Map(Vec<(QueryValue, QueryValue)>),
}

impl QueryValue {
    /// Deterministic total comparison used to canonicalize arbitrary pull
    /// result keys. This is deliberately a query-result ordering rather than
    /// a stored-value index ordering: container shape participates before
    /// recursively comparing contents, while scalar comparison delegates to
    /// Datomic's logical value comparator.
    pub fn canonical_cmp(&self, other: &Self) -> std::cmp::Ordering {
        use std::cmp::Ordering;

        let rank = |value: &Self| match value {
            Self::Nil => 0_u8,
            Self::Scalar(_) => 1,
            Self::Tuple(_) => 2,
            Self::Collection(_) => 3,
            Self::Map(_) => 4,
        };
        let ordering = rank(self).cmp(&rank(other));
        if ordering != Ordering::Equal {
            return ordering;
        }
        match (self, other) {
            (Self::Nil, Self::Nil) => Ordering::Equal,
            (Self::Scalar(left), Self::Scalar(right)) => left.index_cmp(right),
            (Self::Tuple(left), Self::Tuple(right))
            | (Self::Collection(left), Self::Collection(right)) => {
                compare_query_sequences(left, right)
            }
            (Self::Map(left), Self::Map(right)) => compare_query_maps(left, right),
            _ => unreachable!("equal query-value ranks must have matching variants"),
        }
    }

    /// Insert or replace one map entry and restore canonical key order. Pull
    /// maps are semantically unordered, so neither keyword-only ordering nor
    /// selector order may leak into equality or returned representation.
    pub(crate) fn put_map_entry(
        entries: &mut Vec<(QueryValue, QueryValue)>,
        key: QueryValue,
        value: QueryValue,
    ) {
        if let Some((_, existing)) = entries
            .iter_mut()
            .find(|(candidate, _)| candidate.canonical_cmp(&key).is_eq())
        {
            *existing = value;
        } else {
            entries.push((key, value));
            entries.sort_by(|(left, _), (right, _)| left.canonical_cmp(right));
        }
    }
}

impl PartialEq for QueryValue {
    fn eq(&self, other: &Self) -> bool {
        self.canonical_cmp(other).is_eq()
    }
}

impl Eq for QueryValue {}

fn compare_query_sequences(left: &[QueryValue], right: &[QueryValue]) -> std::cmp::Ordering {
    left.iter()
        .zip(right)
        .find_map(|(left, right)| {
            let ordering = left.canonical_cmp(right);
            ordering.is_ne().then_some(ordering)
        })
        .unwrap_or_else(|| left.len().cmp(&right.len()))
}

fn compare_query_maps(
    left: &[(QueryValue, QueryValue)],
    right: &[(QueryValue, QueryValue)],
) -> std::cmp::Ordering {
    let left = sorted_query_map_entries(left);
    let right = sorted_query_map_entries(right);
    left.iter()
        .zip(&right)
        .find_map(|((left_key, left_value), (right_key, right_value))| {
            let ordering = left_key
                .canonical_cmp(right_key)
                .then_with(|| left_value.canonical_cmp(right_value));
            ordering.is_ne().then_some(ordering)
        })
        .unwrap_or_else(|| left.len().cmp(&right.len()))
}

fn sorted_query_map_entries(
    entries: &[(QueryValue, QueryValue)],
) -> Vec<&(QueryValue, QueryValue)> {
    let mut entries = entries.iter().collect::<Vec<_>>();
    entries.sort_by(|(left_key, left_value), (right_key, right_value)| {
        left_key
            .canonical_cmp(right_key)
            .then_with(|| left_value.canonical_cmp(right_value))
    });
    entries
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum QueryResult {
    Relation(Vec<Vec<QueryValue>>),
    Collection(Vec<QueryValue>),
    Tuple(Option<Vec<QueryValue>>),
    Scalar(Option<QueryValue>),
}

#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct QueryStats {
    pub clauses_executed: u64,
    pub datoms_examined: u64,
    pub index_seeks: u64,
    pub rows_produced: u64,
    pub rule_iterations: u64,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct PlanStep {
    pub clause: String,
    pub access: String,
    pub rows_before: usize,
    pub rows_after: usize,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct QueryOutcome {
    pub result: QueryResult,
    pub stats: QueryStats,
    pub plan: Vec<PlanStep>,
}

#[derive(Clone, Debug)]
pub struct QueryControl {
    pub timeout: Option<Duration>,
    pub max_work: usize,
    pub max_intermediate_rows: usize,
    pub max_result_rows: usize,
    pub cancel: Arc<AtomicBool>,
    /// Reference mode used by differential tests to disable bound-prefix
    /// selection and evaluate every data clause through EAVT.
    pub force_scan: bool,
}

impl Default for QueryControl {
    fn default() -> Self {
        Self {
            timeout: None,
            max_work: 10_000_000,
            max_intermediate_rows: 1_000_000,
            max_result_rows: 100_000,
            cancel: Arc::new(AtomicBool::new(false)),
            force_scan: false,
        }
    }
}

pub struct QueryEngine;

type NativeQueryFunction = dyn Fn(&DatabaseValue, &[Value], &QueryControl) -> Result<Vec<Vec<Value>>, SemanticError>
    + Send
    + Sync;

#[derive(Clone)]
struct QueryExtension {
    callback: Arc<NativeQueryFunction>,
    exact_hash: Option<ProgramHash>,
}

#[derive(Clone, Default)]
pub struct QueryExtensions {
    functions: BTreeMap<String, QueryExtension>,
}

impl fmt::Debug for QueryExtensions {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_map()
            .entries(
                self.functions
                    .iter()
                    .map(|(name, function)| (name, function.exact_hash)),
            )
            .finish()
    }
}

impl QueryExtensions {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn register_local<F>(&mut self, name: impl Into<String>, function: F)
    where
        F: Fn(&DatabaseValue, &[Value], &QueryControl) -> Result<Vec<Vec<Value>>, SemanticError>
            + Send
            + Sync
            + 'static,
    {
        self.functions.insert(
            name.into(),
            QueryExtension {
                callback: Arc::new(function),
                exact_hash: None,
            },
        );
    }

    pub fn register_program(
        &mut self,
        name: impl Into<String>,
        hash: ProgramHash,
        program: Program,
    ) -> Result<(), SemanticError> {
        if program.kind != ProgramKind::Query {
            return Err(SemanticError::incorrect(
                "query/not-query-program",
                "persisted query extension must have query program kind",
            ));
        }
        self.functions.insert(
            name.into(),
            QueryExtension {
                callback: Arc::new(move |database, arguments, control| {
                    let program_control = ProgramControl {
                        fuel: control.max_work as u64,
                        max_stack: control.max_intermediate_rows,
                        max_value_bytes: control.max_work,
                        max_collection_items: control.max_intermediate_rows,
                        max_forms: control.max_result_rows,
                        max_output: control.max_work,
                        max_calls: 1,
                        cancelled: Some(control.cancel.as_ref()),
                    };
                    match ProgramRuntime.execute_query(
                        &program,
                        database,
                        arguments,
                        program_control,
                    )? {
                        ProgramOutput::Query(rows) => Ok(rows),
                        _ => unreachable!("program kind was checked"),
                    }
                }),
                exact_hash: Some(hash),
            },
        );
        Ok(())
    }

    fn invoke(
        &self,
        name: &str,
        database: &DatabaseValue,
        arguments: &[Value],
        control: &QueryControl,
    ) -> Result<Vec<Vec<Value>>, SemanticError> {
        let extension = self.functions.get(name).ok_or_else(|| {
            SemanticError::incorrect(
                "query/unknown-extension",
                format!("unknown query extension {name}"),
            )
        })?;
        catch_unwind(AssertUnwindSafe(|| {
            (extension.callback)(database, arguments, control)
        }))
        .map_err(|_| {
            SemanticError::new(
                ErrorCategory::Fault,
                "query/local-extension-panicked",
                format!("query extension {name} panicked"),
            )
        })?
    }

    fn description(&self, name: &str) -> String {
        match self
            .functions
            .get(name)
            .and_then(|function| function.exact_hash)
        {
            Some(hash) => format!(
                "persisted extension {}",
                hash.iter()
                    .map(|byte| format!("{byte:02x}"))
                    .collect::<String>()
            ),
            None => format!("local extension {name}"),
        }
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
enum BoundValue {
    Nil,
    Stored(Value),
}

impl Ord for BoundValue {
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        self.index_cmp(other)
    }
}

impl PartialOrd for BoundValue {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        Some(self.cmp(other))
    }
}

impl BoundValue {
    fn stored(&self) -> Option<&Value> {
        match self {
            Self::Nil => None,
            Self::Stored(value) => Some(value),
        }
    }

    fn index_cmp(&self, other: &Self) -> std::cmp::Ordering {
        match (self, other) {
            (Self::Nil, Self::Nil) => std::cmp::Ordering::Equal,
            (Self::Nil, Self::Stored(_)) => std::cmp::Ordering::Less,
            (Self::Stored(_), Self::Nil) => std::cmp::Ordering::Greater,
            (Self::Stored(left), Self::Stored(right)) => left.index_cmp(right),
        }
    }

    fn query_value(&self) -> QueryValue {
        match self {
            Self::Nil => QueryValue::Nil,
            Self::Stored(value) => QueryValue::Scalar(value.clone()),
        }
    }
}

impl From<Value> for BoundValue {
    fn from(value: Value) -> Self {
        Self::Stored(value)
    }
}

type Row = BTreeMap<Variable, BoundValue>;

#[derive(Clone, Debug, Eq, Ord, PartialEq, PartialOrd)]
struct RuleInvocationKey {
    source: String,
    name: String,
    bindings: Vec<Option<BoundValue>>,
}

struct State<'a> {
    sources: BTreeMap<&'a str, &'a DatabaseValue>,
    control: &'a QueryControl,
    deadline: Option<Instant>,
    work: usize,
    stats: QueryStats,
    plan: Vec<PlanStep>,
    extensions: Option<&'a QueryExtensions>,
    rule_memo: BTreeMap<RuleInvocationKey, Vec<Vec<BoundValue>>>,
    solving_rules: bool,
}

impl QueryEngine {
    pub fn execute(
        query: &Query,
        sources: &[QuerySource],
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        Self::execute_with_extensions(query, sources, inputs, control, None)
    }

    pub fn execute_with_extensions(
        query: &Query,
        sources: &[QuerySource],
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: Option<&QueryExtensions>,
    ) -> Result<QueryOutcome, SemanticError> {
        validate_query(query, inputs)?;
        let mut source_map = BTreeMap::new();
        for source in sources {
            if source_map
                .insert(source.name.as_str(), &source.database)
                .is_some()
            {
                return Err(SemanticError::incorrect(
                    "query/duplicate-source",
                    format!("source {} is supplied more than once", source.name),
                ));
            }
        }
        validate_consumed_sources(query, &source_map)?;
        let deadline = control.timeout.map(|timeout| Instant::now() + timeout);
        let mut state = State {
            sources: source_map,
            control,
            deadline,
            work: 0,
            stats: QueryStats::default(),
            plan: Vec::new(),
            extensions,
            rule_memo: BTreeMap::new(),
            solving_rules: false,
        };
        let initial = bind_inputs(&query.inputs, inputs)?;
        let rows = evaluate_clauses(&query.clauses, initial, &query.rules, None, &mut state)?;
        let mut pull_budget = QueryPullBudget::new(
            Arc::clone(&control.cancel),
            state.deadline,
            control.max_work,
            state.work,
        );
        let result = shape_results(
            query,
            rows,
            control.max_result_rows,
            &state.sources,
            &mut pull_budget,
        )?;
        state.work = pull_budget.work();
        state.stats.rows_produced = result_len(&result) as u64;
        Ok(QueryOutcome {
            result,
            stats: state.stats,
            plan: state.plan,
        })
    }
}

fn validate_consumed_sources(
    query: &Query,
    sources: &BTreeMap<&str, &DatabaseValue>,
) -> Result<(), SemanticError> {
    let mut consumed = BTreeSet::new();
    let mut visited_rules = BTreeSet::new();
    collect_consumed_sources(
        &query.clauses,
        &query.rules,
        None,
        &mut consumed,
        &mut visited_rules,
    );
    for element in find_elements(&query.find) {
        if let FindElement::Pull { source, .. } = element {
            consumed.insert(source.clone());
        }
    }
    if let Some(source) = consumed
        .into_iter()
        .find(|source| !sources.contains_key(source.as_str()))
    {
        return Err(SemanticError::incorrect(
            "query/unknown-source",
            format!("unknown source {source}"),
        ));
    }
    for element in find_elements(&query.find) {
        if let FindElement::Pull { source, .. } = element {
            sources
                .get(source.as_str())
                .expect("pull source existence was validated")
                .require_point_in_time("pull")?;
        }
    }
    Ok(())
}

fn collect_consumed_sources(
    clauses: &[Clause],
    rules: &[Rule],
    inherited_source: Option<&str>,
    consumed: &mut BTreeSet<String>,
    visited_rules: &mut BTreeSet<(String, String)>,
) {
    for clause in clauses {
        match clause {
            Clause::Pattern(pattern) => {
                consumed.insert(effective_source(&pattern.source, inherited_source).to_owned());
            }
            Clause::Predicate {
                predicate: Predicate::Missing,
                source,
                ..
            }
            | Clause::Function {
                function: Function::GetElse | Function::GetSome | Function::Extension(_),
                source,
                ..
            } => {
                consumed.insert(effective_source(source, inherited_source).to_owned());
            }
            Clause::Not { clauses, .. } => {
                collect_consumed_sources(clauses, rules, inherited_source, consumed, visited_rules)
            }
            Clause::Or { branches, .. } => {
                for branch in branches {
                    collect_consumed_sources(
                        branch,
                        rules,
                        inherited_source,
                        consumed,
                        visited_rules,
                    );
                }
            }
            Clause::Rule { source, name, .. } => {
                let source = effective_source(source, inherited_source).to_owned();
                if visited_rules.insert((name.clone(), source.clone())) {
                    for rule in rules.iter().filter(|rule| rule.name == *name) {
                        collect_consumed_sources(
                            &rule.clauses,
                            rules,
                            Some(&source),
                            consumed,
                            visited_rules,
                        );
                    }
                }
            }
            Clause::Predicate { .. } | Clause::Function { .. } => {}
        }
    }
}

impl DatabaseValue {
    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        QueryEngine::execute(
            query,
            &[QuerySource {
                name: "$".into(),
                database: self.clone(),
            }],
            inputs,
            control,
        )
    }

    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        QueryEngine::execute_with_extensions(
            query,
            &[QuerySource {
                name: "$".into(),
                database: self.clone(),
            }],
            inputs,
            control,
            Some(extensions),
        )
    }
}

impl Database {
    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value().query(query, inputs, control)
    }

    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value()
            .query_with_extensions(query, inputs, control, extensions)
    }
}

fn validate_query(query: &Query, inputs: &[QueryInput]) -> Result<(), SemanticError> {
    if query.clauses.is_empty() && query.inputs.is_empty() {
        return Err(SemanticError::incorrect(
            "query/no-input-or-where",
            "query requires input bindings or where clauses",
        ));
    }
    if query.inputs.len() != inputs.len() {
        return Err(SemanticError::incorrect(
            "query/input-arity",
            format!(
                "expected {} inputs, got {}",
                query.inputs.len(),
                inputs.len()
            ),
        ));
    }
    validate_ground_clauses(&query.clauses)?;
    for rule in &query.rules {
        if rule.name.is_empty()
            || rule.head.is_empty()
            || rule.required.iter().any(|index| *index >= rule.head.len())
        {
            return Err(SemanticError::incorrect(
                "query/invalid-rule-head",
                "rule heads need a name, arguments, and valid required indexes",
            ));
        }
        validate_ground_clauses(&rule.clauses)?;
    }
    let mut pulled = BTreeSet::new();
    for element in find_elements(&query.find) {
        if let FindElement::Pull { variable, .. } = element
            && !pulled.insert(variable)
        {
            return Err(SemanticError::incorrect(
                "query/duplicate-pull-variable",
                format!(
                    "{} appears in more than one pull expression",
                    variable.name()
                ),
            ));
        }
    }
    let mut signatures = BTreeMap::new();
    for rule in &query.rules {
        if let Some((arity, required)) = signatures.get(rule.name.as_str()) {
            if *arity != rule.head.len() {
                return Err(SemanticError::incorrect(
                    "query/rule-arity",
                    format!("rule {} has inconsistent arity", rule.name),
                ));
            }
            if *required != &rule.required {
                return Err(SemanticError::incorrect(
                    "query/rule-required-mismatch",
                    format!("rule {} has inconsistent required bindings", rule.name),
                ));
            }
        } else {
            signatures.insert(rule.name.as_str(), (rule.head.len(), &rule.required));
        }
    }
    Ok(())
}

fn validate_ground_clauses(clauses: &[Clause]) -> Result<(), SemanticError> {
    for clause in clauses {
        match clause {
            Clause::Function {
                function: Function::Tuple,
                args,
                ..
            } if args.is_empty() => {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "tuple requires one or more arguments",
                ));
            }
            Clause::Function {
                function: Function::Ground,
                args,
                ..
            } => {
                if args.len() != 1 {
                    return Err(SemanticError::incorrect(
                        "query/function-arity",
                        "ground requires exactly one constant",
                    ));
                }
                if !matches!(args[0], Term::Constant(_) | Term::Nil) {
                    return Err(SemanticError::incorrect(
                        "query/ground-not-constant",
                        "ground requires a constant argument",
                    ));
                }
            }
            Clause::Not { clauses, .. } => validate_ground_clauses(clauses)?,
            Clause::Or { branches, .. } => {
                for branch in branches {
                    validate_ground_clauses(branch)?;
                }
            }
            _ => {}
        }
    }
    Ok(())
}

fn bind_inputs(specs: &[InputSpec], values: &[QueryInput]) -> Result<Vec<Row>, SemanticError> {
    let mut rows = vec![Row::new()];
    for (spec, value) in specs.iter().zip(values) {
        let relation = match (spec, value) {
            (InputSpec::Scalar(variable), QueryInput::Scalar(value)) => {
                vec![vec![(Some(variable), value)]]
            }
            (InputSpec::Tuple(vars), QueryInput::Tuple(values)) if vars.len() == values.len() => {
                vec![
                    vars.iter()
                        .zip(values)
                        .map(|(var, value)| (var.as_ref(), value))
                        .collect(),
                ]
            }
            (InputSpec::Collection(variable), QueryInput::Collection(values)) => values
                .iter()
                .map(|value| vec![(Some(variable), value)])
                .collect(),
            (InputSpec::Relation(vars), QueryInput::Relation(value_rows)) => {
                if value_rows.iter().any(|row| row.len() != vars.len()) {
                    return Err(SemanticError::incorrect(
                        "query/input-shape",
                        "relation input row has the wrong width",
                    ));
                }
                value_rows
                    .iter()
                    .map(|row| {
                        vars.iter()
                            .zip(row)
                            .map(|(var, value)| (var.as_ref(), value))
                            .collect()
                    })
                    .collect()
            }
            _ => {
                return Err(SemanticError::incorrect(
                    "query/input-shape",
                    "query input does not match its binding form",
                ));
            }
        };
        let mut next = Vec::new();
        for row in &rows {
            for bindings in &relation {
                let mut candidate = row.clone();
                let mut valid = true;
                for (variable, value) in bindings {
                    if let Some(variable) = variable
                        && !unify_variable(
                            &mut candidate,
                            variable,
                            &BoundValue::Stored((*value).clone()),
                        )
                    {
                        valid = false;
                        break;
                    }
                }
                if valid {
                    push_unique_row(&mut next, candidate);
                }
            }
        }
        rows = next;
    }
    Ok(rows)
}

fn evaluate_clauses(
    clauses: &[Clause],
    mut rows: Vec<Row>,
    rules: &[Rule],
    inherited_source: Option<&str>,
    state: &mut State<'_>,
) -> Result<Vec<Row>, SemanticError> {
    let mut remaining: Vec<_> = clauses.iter().collect();
    while !remaining.is_empty() {
        state.check(1)?;
        let sample = rows.first().cloned().unwrap_or_default();
        let selected = remaining
            .iter()
            .enumerate()
            .filter(|(_, clause)| clause_ready(clause, &sample, rules))
            .max_by_key(|(_, clause)| clause_score(clause, &sample))
            .map(|(index, _)| index)
            .ok_or_else(|| {
                SemanticError::incorrect(
                    "query/insufficient-binding",
                    "no remaining clause has its required variables bound",
                )
            })?;
        let clause = remaining.remove(selected);
        let before = rows.len();
        let (next, access) = evaluate_clause(clause, rows, rules, inherited_source, state)?;
        rows = dedupe_rows(next);
        if rows.len() > state.control.max_intermediate_rows {
            return Err(resource(
                "query/intermediate-limit",
                "query intermediate relation exceeded its row limit",
            ));
        }
        state.stats.clauses_executed += 1;
        state.plan.push(PlanStep {
            clause: clause_name(clause).into(),
            access,
            rows_before: before,
            rows_after: rows.len(),
        });
        if rows.is_empty() {
            break;
        }
    }
    Ok(rows)
}

fn evaluate_clause(
    clause: &Clause,
    rows: Vec<Row>,
    rules: &[Rule],
    inherited_source: Option<&str>,
    state: &mut State<'_>,
) -> Result<(Vec<Row>, String), SemanticError> {
    match clause {
        Clause::Pattern(pattern) => evaluate_pattern(pattern, rows, inherited_source, state),
        Clause::Predicate {
            predicate,
            source,
            args,
        } => {
            let mut next = Vec::new();
            for row in rows {
                state.check(1)?;
                let values = resolve_args(args, &row)?;
                if evaluate_predicate(
                    *predicate,
                    effective_source(source, inherited_source),
                    &values,
                    state,
                )? {
                    next.push(row);
                }
            }
            Ok((next, "predicate".into()))
        }
        Clause::Function {
            function,
            source,
            args,
            binding,
        } => {
            let mut next = Vec::new();
            for row in rows {
                state.check(1)?;
                let values = resolve_args(args, &row)?;
                for produced in evaluate_function(
                    function.clone(),
                    effective_source(source, inherited_source),
                    &values,
                    binding,
                    state,
                )? {
                    for bound in bind_output(&row, binding, &produced)? {
                        push_unique_row(&mut next, bound);
                    }
                }
            }
            let access = match function {
                Function::Extension(name) => state.extensions.map_or_else(
                    || format!("extension {name}"),
                    |registry| registry.description(name),
                ),
                _ => "function".into(),
            };
            Ok((next, access))
        }
        Clause::Not { join, clauses } => {
            let required = join
                .clone()
                .unwrap_or_else(|| variables_in_clauses(clauses));
            let mut next = Vec::new();
            for row in rows {
                if required.iter().any(|variable| !row.contains_key(variable)) {
                    return Err(SemanticError::incorrect(
                        "query/insufficient-binding",
                        "not clause has unbound join variables",
                    ));
                }
                let seed = if join.is_some() {
                    project_row(&row, &required)
                } else {
                    row.clone()
                };
                if evaluate_clauses(clauses, vec![seed], rules, inherited_source, state)?.is_empty()
                {
                    next.push(row);
                }
            }
            Ok((next, "set-difference".into()))
        }
        Clause::Or { join, branches } => {
            validate_or(branches, join.as_deref())?;
            let mut next = Vec::new();
            for row in rows {
                for branch in branches {
                    let seed = join
                        .as_ref()
                        .map_or_else(|| row.clone(), |variables| project_row(&row, variables));
                    for produced in
                        evaluate_clauses(branch, vec![seed], rules, inherited_source, state)?
                    {
                        if let Some(join) = join {
                            let mut merged = row.clone();
                            if join.iter().all(|variable| {
                                produced.get(variable).is_none_or(|value| {
                                    unify_variable(&mut merged, variable, value)
                                })
                            }) {
                                push_unique_row(&mut next, merged);
                            }
                        } else {
                            push_unique_row(&mut next, produced);
                        }
                    }
                }
            }
            Ok((next, "set-union".into()))
        }
        Clause::Rule { source, name, args } => {
            let source = effective_source(source, inherited_source);
            let (arity, required) = rule_signature(rules, name)?;
            if args.len() != arity {
                return Err(SemanticError::incorrect(
                    "query/rule-arity",
                    format!("rule {name} expects {arity} arguments, got {}", args.len()),
                ));
            }
            let mut next = Vec::new();
            for row in rows {
                if required
                    .iter()
                    .any(|index| !term_is_bound(&args[*index], &row))
                {
                    return Err(SemanticError::incorrect(
                        "query/insufficient-binding",
                        format!("rule {name} requires its declared input positions to be bound"),
                    ));
                }
                let key = RuleInvocationKey {
                    source: source.to_owned(),
                    name: name.clone(),
                    bindings: args
                        .iter()
                        .map(|term| term_bound_value(term, &row))
                        .collect(),
                };
                let relation = solve_rule_invocation(key, rules, state)?;
                for tuple in &relation {
                    if tuple.len() != args.len() {
                        return Err(fault(
                            "query/rule-relation-width",
                            "compiled rule relation has the wrong width",
                        ));
                    }
                    let mut candidate = row.clone();
                    if args
                        .iter()
                        .zip(tuple)
                        .all(|(term, value)| unify_term(&mut candidate, term, value))
                    {
                        push_unique_row(&mut next, candidate);
                    }
                }
            }
            Ok((next, "rule-relation".into()))
        }
    }
}

fn evaluate_pattern(
    pattern: &DataPattern,
    rows: Vec<Row>,
    inherited_source: Option<&str>,
    state: &mut State<'_>,
) -> Result<(Vec<Row>, String), SemanticError> {
    let source = effective_source(&pattern.source, inherited_source);
    let database = state.sources.get(source).copied().ok_or_else(|| {
        SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
    })?;
    let mut next = Vec::new();
    let mut access = "EAVT scan".to_owned();
    for row in rows {
        state.check(1)?;
        let entity = resolve_entity(database, &pattern.entity, &row)?;
        // A bound lookup ref that does not resolve denotes no entity.  Keep it
        // distinct from a blank or unbound entity term, which intentionally
        // leaves the E position open for an index scan.
        if entity.is_none() && term_is_bound(&pattern.entity, &row) {
            continue;
        }
        let attribute = resolve_attribute(database, &pattern.attribute, &row)?;
        let bound_value = term_bound_value(&pattern.value, &row);
        let value = bound_value
            .as_ref()
            .and_then(BoundValue::stored)
            .map(|value| resolve_pattern_value(database, attribute, value))
            .transpose()?;
        let (datoms, selected) = select_datoms(
            database,
            entity,
            attribute,
            value.as_ref(),
            state.control.force_scan,
        )?;
        access = selected;
        if access != "EAVT scan" {
            state.stats.index_seeks += 1;
        }
        for datom in datoms {
            let datom = datom?;
            state.check(1)?;
            state.stats.datoms_examined = state.stats.datoms_examined.saturating_add(1);
            let mut candidate = row.clone();
            if unify_entity_term(
                database,
                &mut candidate,
                &pattern.entity,
                entity,
                datom.entity,
            )? && unify_attribute_term(
                database,
                &mut candidate,
                &pattern.attribute,
                attribute,
                datom.attribute,
            )? && unify_value_term(
                database,
                &mut candidate,
                &pattern.value,
                attribute,
                value.as_ref(),
                &datom.value,
            )? && pattern.transaction.as_ref().is_none_or(|term| {
                unify_term(
                    &mut candidate,
                    term,
                    &BoundValue::Stored(Value::Ref(datom.tx)),
                )
            }) && pattern.added.as_ref().is_none_or(|term| {
                unify_term(
                    &mut candidate,
                    term,
                    &BoundValue::Stored(Value::Bool(datom.added)),
                )
            }) {
                push_unique_row(&mut next, candidate);
            }
        }
    }
    Ok((next, access))
}

fn select_datoms<'a>(
    database: &'a DatabaseValue,
    entity: Option<u64>,
    attribute: Option<u32>,
    value: Option<&Value>,
    force_scan: bool,
) -> Result<
    (
        Box<dyn Iterator<Item = Result<crate::Datom, SemanticError>> + 'a>,
        String,
    ),
    SemanticError,
> {
    if !force_scan {
        if let Some(entity) = entity {
            let prefix = IndexPrefix::Eavt {
                entity,
                attribute,
                value: attribute.and(value).cloned(),
            };
            return Ok((
                Box::new(database.datoms_with_prefix(&prefix)?.into_iter().map(Ok)),
                "EAVT seek".into(),
            ));
        }
        if let Some(attribute) = attribute {
            if let Some(value) = value
                && database
                    .schema()
                    .attribute(attribute)
                    .is_ok_and(|a| a.indexed || a.unique.is_some())
            {
                let prefix = IndexPrefix::Avet {
                    attribute,
                    value: Some(value.clone()),
                    entity: None,
                };
                return Ok((
                    Box::new(database.datoms_with_prefix(&prefix)?.into_iter().map(Ok)),
                    "AVET seek".into(),
                ));
            }
            let prefix = IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            };
            return Ok((
                Box::new(database.datoms_with_prefix(&prefix)?.into_iter().map(Ok)),
                "AEVT seek".into(),
            ));
        }
        if let Some(Value::Ref(referenced)) = value {
            let prefix = IndexPrefix::Vaet {
                value: Value::Ref(*referenced),
                attribute: None,
                entity: None,
            };
            return Ok((
                Box::new(database.datoms_with_prefix(&prefix)?.into_iter().map(Ok)),
                "VAET seek".into(),
            ));
        }
    }
    Ok((
        Box::new(database.query_scan_cursor(IndexOrder::Eavt)?),
        "EAVT scan".into(),
    ))
}

fn clause_ready(clause: &Clause, row: &Row, rules: &[Rule]) -> bool {
    match clause {
        Clause::Pattern(_) | Clause::Or { .. } => true,
        Clause::Predicate { args, .. } | Clause::Function { args, .. } => args
            .iter()
            .all(|term| !matches!(term, Term::Variable(variable) if !row.contains_key(variable))),
        Clause::Not { join, clauses } => join
            .clone()
            .unwrap_or_else(|| variables_in_clauses(clauses))
            .iter()
            .all(|variable| row.contains_key(variable)),
        Clause::Rule { name, args, .. } => {
            match rule_signature(rules, name) {
                Ok((arity, required)) => {
                    args.len() != arity
                        || required
                            .iter()
                            .all(|index| term_is_bound(&args[*index], row))
                }
                // Let evaluation produce the precise unknown-rule anomaly.
                Err(_) => true,
            }
        }
    }
}

fn clause_score(clause: &Clause, row: &Row) -> usize {
    match clause {
        Clause::Pattern(pattern) => {
            [&pattern.entity, &pattern.attribute, &pattern.value]
                .into_iter()
                .filter(|term| term_is_bound(term, row))
                .count()
                * 10
        }
        Clause::Predicate { .. } | Clause::Not { .. } => 40,
        Clause::Function { .. } => 30,
        Clause::Rule { .. } => 20,
        Clause::Or { .. } => 10,
    }
}

fn rule_signature<'a>(
    rules: &'a [Rule],
    name: &str,
) -> Result<(usize, &'a BTreeSet<usize>), SemanticError> {
    rules
        .iter()
        .find(|rule| rule.name == name)
        .map(|rule| (rule.head.len(), &rule.required))
        .ok_or_else(|| {
            SemanticError::incorrect("query/unknown-rule", format!("unknown rule {name}"))
        })
}

fn solve_rule_invocation(
    key: RuleInvocationKey,
    rules: &[Rule],
    state: &mut State<'_>,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    // Recovered `eval-query` keys its input work by source and adorned
    // predicate, accumulates answers in `ans`, and the outer `qsqr` loop
    // repeats until answer cardinalities stop changing. This memo uses the
    // concrete bound values as well as the adornment: it is more selective,
    // while retaining the same monotone fixed-point boundary.
    let (arity, required) = rule_signature(rules, &key.name)?;
    if key.bindings.len() != arity {
        return Err(SemanticError::incorrect(
            "query/rule-arity",
            format!(
                "rule {} expects {arity} arguments, got {}",
                key.name,
                key.bindings.len()
            ),
        ));
    }
    if required.iter().any(|index| key.bindings[*index].is_none()) {
        return Err(SemanticError::incorrect(
            "query/insufficient-binding",
            format!(
                "rule {} requires its declared input positions to be bound",
                key.name
            ),
        ));
    }

    ensure_rule_memo_entry(state, key.clone());
    if !state.solving_rules {
        state.solving_rules = true;
        let result = stabilize_rule_memo(rules, state);
        state.solving_rules = false;
        result?;
    }
    Ok(rule_memo_rows(state, &key))
}

fn stabilize_rule_memo(rules: &[Rule], state: &mut State<'_>) -> Result<(), SemanticError> {
    loop {
        state.check(1)?;
        let before_entries = state.rule_memo.len();
        let before_rows = state.rule_memo.values().map(Vec::len).sum::<usize>();
        let keys = state.rule_memo.keys().cloned().collect::<Vec<_>>();

        for key in keys {
            let produced = evaluate_rule_key(&key, rules, state)?;
            ensure_rule_memo_entry(state, key.clone());
            for tuple in produced {
                let is_new = !state
                    .rule_memo
                    .get(&key)
                    .is_some_and(|rows| rows.contains(&tuple));
                if is_new {
                    state.check(1)?;
                    let rows = state
                        .rule_memo
                        .get_mut(&key)
                        .expect("rule memo entry was ensured");
                    rows.push(tuple);
                    if rows.len() > state.control.max_intermediate_rows {
                        return Err(resource(
                            "query/intermediate-limit",
                            "rule memo relation exceeded the intermediate row limit",
                        ));
                    }
                }
            }
        }

        state.stats.rule_iterations += 1;
        let after_rows = state.rule_memo.values().map(Vec::len).sum::<usize>();
        if state.rule_memo.len() == before_entries && after_rows == before_rows {
            return Ok(());
        }
    }
}

fn evaluate_rule_key(
    key: &RuleInvocationKey,
    rules: &[Rule],
    state: &mut State<'_>,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    let mut produced = Vec::new();
    for rule in rules.iter().filter(|rule| rule.name == key.name) {
        let mut seed = Row::new();
        let mut compatible = true;
        for (variable, value) in rule.head.iter().zip(&key.bindings) {
            if let Some(value) = value
                && !unify_variable(&mut seed, variable, value)
            {
                compatible = false;
                break;
            }
        }
        if !compatible {
            continue;
        }
        let rows = evaluate_clauses(&rule.clauses, vec![seed], rules, Some(&key.source), state)?;
        for row in rows {
            let tuple = rule
                .head
                .iter()
                .map(|variable| {
                    row.get(variable).cloned().ok_or_else(|| {
                        SemanticError::incorrect(
                            "query/unbound-rule-head",
                            format!("rule {} did not bind {}", rule.name, variable.name()),
                        )
                    })
                })
                .collect::<Result<Vec<_>, _>>()?;
            if !produced.contains(&tuple) {
                produced.push(tuple);
            }
        }
    }
    Ok(produced)
}

fn ensure_rule_memo_entry(state: &mut State<'_>, key: RuleInvocationKey) {
    state.rule_memo.entry(key).or_default();
}

fn rule_memo_rows(state: &State<'_>, key: &RuleInvocationKey) -> Vec<Vec<BoundValue>> {
    state.rule_memo.get(key).cloned().unwrap_or_default()
}

fn evaluate_predicate(
    predicate: Predicate,
    source: &str,
    args: &[BoundValue],
    state: &State<'_>,
) -> Result<bool, SemanticError> {
    match predicate {
        Predicate::Eq
        | Predicate::NotEq
        | Predicate::Less
        | Predicate::LessOrEqual
        | Predicate::Greater
        | Predicate::GreaterOrEqual => {
            if args.len() != 2 {
                return Err(SemanticError::incorrect(
                    "query/predicate-arity",
                    "comparison predicates require two arguments",
                ));
            }
            let ordering = args[0].index_cmp(&args[1]);
            Ok(match predicate {
                Predicate::Eq => ordering.is_eq(),
                Predicate::NotEq => !ordering.is_eq(),
                Predicate::Less => ordering.is_lt(),
                Predicate::LessOrEqual => !ordering.is_gt(),
                Predicate::Greater => ordering.is_gt(),
                Predicate::GreaterOrEqual => !ordering.is_lt(),
                Predicate::Missing => unreachable!(),
            })
        }
        Predicate::Missing => {
            if args.len() != 2 {
                return Err(SemanticError::incorrect(
                    "query/predicate-arity",
                    "missing requires entity and attribute",
                ));
            }
            let database = state.sources.get(source).copied().ok_or_else(|| {
                SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
            })?;
            let entity = entity_value(database, require_stored(&args[0], "query/entity-value")?)?
                .ok_or_else(|| {
                SemanticError::incorrect("query/entity-value", "missing entity must be a ref")
            })?;
            let attribute =
                attribute_value(database, require_stored(&args[1], "query/attribute-value")?)?;
            Ok(database.values(entity, attribute)?.is_empty())
        }
    }
}

fn evaluate_function(
    function: Function,
    source: &str,
    args: &[BoundValue],
    binding: &Binding,
    state: &State<'_>,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    match function {
        Function::Ground => ground_output(args, binding),
        Function::Tuple => Ok(vec![vec![BoundValue::Stored(Value::Tuple(
            args.iter().map(|value| value.stored().cloned()).collect(),
        ))]]),
        Function::Untuple => match args {
            [BoundValue::Stored(Value::Tuple(values))] => Ok(vec![
                values
                    .iter()
                    .cloned()
                    .map(|value| value.map_or(BoundValue::Nil, BoundValue::Stored))
                    .collect(),
            ]),
            _ => Err(SemanticError::incorrect(
                "query/function-type",
                "untuple requires one tuple",
            )),
        },
        Function::Add | Function::Subtract | Function::Multiply | Function::Divide => {
            numeric_function(function, args).map(|value| vec![vec![BoundValue::Stored(value)]])
        }
        Function::GetElse => {
            if args.len() != 3 {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "get-else requires entity, attribute, and default",
                ));
            }
            let database = source_database(state, source)?;
            let entity = entity_value(database, require_stored(&args[0], "query/entity-value")?)?
                .ok_or_else(|| {
                SemanticError::incorrect("query/entity-value", "get-else entity must be a ref")
            })?;
            let attribute =
                attribute_value(database, require_stored(&args[1], "query/attribute-value")?)?;
            if database.schema().attribute(attribute)?.cardinality != crate::Cardinality::One {
                return Err(SemanticError::incorrect(
                    "query/get-else-cardinality",
                    "get-else requires cardinality one",
                ));
            }
            let default = require_stored(&args[2], "query/get-else-nil-default")?.clone();
            let values = database.values(entity, attribute)?;
            Ok(vec![vec![BoundValue::Stored(
                values.first().cloned().unwrap_or(default),
            )]])
        }
        Function::GetSome => {
            if args.len() < 2 {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "get-some requires entity and attributes",
                ));
            }
            let database = source_database(state, source)?;
            let entity = entity_value(database, require_stored(&args[0], "query/entity-value")?)?
                .ok_or_else(|| {
                SemanticError::incorrect("query/entity-value", "get-some entity must be a ref")
            })?;
            let attributes = args[1..]
                .iter()
                .map(|value| {
                    let attribute =
                        attribute_value(database, require_stored(value, "query/attribute-value")?)?;
                    if database.schema().attribute(attribute)?.cardinality
                        != crate::Cardinality::One
                    {
                        return Err(SemanticError::incorrect(
                            "query/get-some-cardinality",
                            "get-some requires cardinality-one attributes",
                        ));
                    }
                    Ok(attribute)
                })
                .collect::<Result<Vec<_>, _>>()?;
            for attribute in attributes {
                if let Some(found) = database.values(entity, attribute)?.first() {
                    return Ok(vec![vec![
                        BoundValue::Stored(Value::Ref(u64::from(attribute))),
                        BoundValue::Stored(found.clone()),
                    ]]);
                }
            }
            Ok(Vec::new())
        }
        Function::Extension(name) => {
            let database = source_database(state, source)?;
            let extensions = state.extensions.ok_or_else(|| {
                SemanticError::incorrect(
                    "query/unknown-extension",
                    format!("no query extension registry supplies {name}"),
                )
            })?;
            let extension_args = args
                .iter()
                .map(|value| require_stored(value, "query/nil-extension-arg").cloned())
                .collect::<Result<Vec<_>, _>>()?;
            let rows = extensions.invoke(&name, database, &extension_args, state.control)?;
            if rows.len() > state.control.max_result_rows
                || rows.iter().map(Vec::len).sum::<usize>() > state.control.max_intermediate_rows
            {
                return Err(resource(
                    "query/extension-output-limit",
                    "query extension exceeded its output limits",
                ));
            }
            Ok(rows
                .into_iter()
                .map(|row| row.into_iter().map(BoundValue::Stored).collect())
                .collect())
        }
    }
}

fn ground_output(
    args: &[BoundValue],
    binding: &Binding,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    let [constant] = args else {
        return Err(SemanticError::incorrect(
            "query/function-arity",
            "ground requires exactly one constant",
        ));
    };
    match binding {
        Binding::Scalar(_) | Binding::Collection(_) => Ok(vec![vec![constant.clone()]]),
        Binding::Tuple(_) => match constant {
            BoundValue::Stored(Value::Tuple(values)) => Ok(vec![
                values
                    .iter()
                    .cloned()
                    .map(|value| value.map_or(BoundValue::Nil, BoundValue::Stored))
                    .collect(),
            ]),
            _ => Err(SemanticError::incorrect(
                "query/function-binding",
                "ground tuple binding requires one tuple constant",
            )),
        },
        Binding::Relation(_) => match constant {
            BoundValue::Stored(Value::Tuple(rows)) => rows
                .iter()
                .map(|row| match row {
                    Some(Value::Tuple(values)) => Ok(values
                        .iter()
                        .cloned()
                        .map(|value| value.map_or(BoundValue::Nil, BoundValue::Stored))
                        .collect()),
                    _ => Err(SemanticError::incorrect(
                        "query/function-binding",
                        "ground relation binding requires a collection of tuple constants",
                    )),
                })
                .collect(),
            _ => Err(SemanticError::incorrect(
                "query/function-binding",
                "ground relation binding requires a collection of tuple constants",
            )),
        },
    }
}

fn require_stored<'a>(
    value: &'a BoundValue,
    code: &'static str,
) -> Result<&'a Value, SemanticError> {
    value.stored().ok_or_else(|| {
        SemanticError::incorrect(code, "nil is not valid for this query function argument")
    })
}

fn numeric_function(function: Function, args: &[BoundValue]) -> Result<Value, SemanticError> {
    if args.len() != 2 {
        return Err(SemanticError::incorrect(
            "query/function-arity",
            "arithmetic functions require two arguments",
        ));
    }
    let left = require_stored(&args[0], "query/numeric-type")?;
    let right = require_stored(&args[1], "query/numeric-type")?;
    match (left, right) {
        (Value::Long(left), Value::Long(right)) => {
            let result = match function {
                Function::Add => left.checked_add(*right),
                Function::Subtract => left.checked_sub(*right),
                Function::Multiply => left.checked_mul(*right),
                Function::Divide if *right != 0 => left.checked_div(*right),
                _ => None,
            }
            .ok_or_else(|| {
                SemanticError::incorrect(
                    "query/arithmetic",
                    "integer arithmetic overflow or division by zero",
                )
            })?;
            Ok(Value::Long(result))
        }
        _ => {
            let left = as_f64(left)?;
            let right = as_f64(right)?;
            if function == Function::Divide && right == 0.0 {
                return Err(SemanticError::incorrect(
                    "query/arithmetic",
                    "division by zero",
                ));
            }
            Ok(Value::Double(match function {
                Function::Add => left + right,
                Function::Subtract => left - right,
                Function::Multiply => left * right,
                Function::Divide => left / right,
                _ => unreachable!(),
            }))
        }
    }
}

fn bind_output(
    row: &Row,
    binding: &Binding,
    output: &[BoundValue],
) -> Result<Vec<Row>, SemanticError> {
    if let Binding::Collection(variable) = binding {
        let [BoundValue::Stored(Value::Tuple(values))] = output else {
            return Err(SemanticError::incorrect(
                "query/function-binding",
                "collection binding requires one tuple value",
            ));
        };
        let mut rows = Vec::new();
        for value in values {
            let value = value.clone().map_or(BoundValue::Nil, BoundValue::Stored);
            let mut candidate = row.clone();
            if unify_variable(&mut candidate, variable, &value) {
                push_unique_row(&mut rows, candidate);
            }
        }
        return Ok(rows);
    }
    let bindings: Vec<(Option<&Variable>, &BoundValue)> = match binding {
        Binding::Scalar(variable) if output.len() == 1 => vec![(Some(variable), &output[0])],
        Binding::Tuple(variables) | Binding::Relation(variables)
            if variables.len() == output.len() =>
        {
            variables
                .iter()
                .zip(output)
                .map(|(variable, value)| (variable.as_ref(), value))
                .collect()
        }
        Binding::Collection(_) => unreachable!(),
        _ => {
            return Err(SemanticError::incorrect(
                "query/function-binding",
                "function output does not match its binding form",
            ));
        }
    };
    let mut candidate = row.clone();
    for (variable, value) in bindings {
        if let Some(variable) = variable
            && !unify_variable(&mut candidate, variable, value)
        {
            return Ok(Vec::new());
        }
    }
    Ok(vec![candidate])
}

fn shape_results(
    query: &Query,
    rows: Vec<Row>,
    max: usize,
    sources: &BTreeMap<&str, &DatabaseValue>,
    pull_budget: &mut QueryPullBudget,
) -> Result<QueryResult, SemanticError> {
    let elements = match &query.find {
        FindSpec::Relation(elements) | FindSpec::Tuple(elements) => elements.as_slice(),
        FindSpec::Collection(element) | FindSpec::Scalar(element) => std::slice::from_ref(element),
    };
    let basis_variables = find_variables(elements)
        .into_iter()
        .chain(query.with.iter().cloned())
        .collect::<Vec<_>>();
    let mut basis = Vec::new();
    for row in rows {
        let projected = basis_variables
            .iter()
            .map(|variable| {
                row.get(variable).cloned().ok_or_else(|| {
                    SemanticError::incorrect(
                        "query/unbound-find-variable",
                        format!("find variable {} is unbound", variable.name()),
                    )
                })
            })
            .collect::<Result<Vec<_>, _>>()?;
        if !basis.contains(&projected) {
            basis.push(projected);
        }
    }
    let mut output = if elements
        .iter()
        .any(|element| matches!(element, FindElement::Aggregate { .. }))
    {
        aggregate_rows(elements, &basis_variables, &basis, sources, pull_budget)?
    } else {
        basis
            .into_iter()
            .map(|row| {
                elements
                    .iter()
                    .map(|element| {
                        project_element(element, &basis_variables, &row, sources, pull_budget)
                    })
                    .collect::<Result<Vec<_>, _>>()
            })
            .collect::<Result<Vec<_>, _>>()?
    };
    dedupe_query_rows(&mut output, query.with.is_empty());
    if output.len() > max {
        return Err(resource(
            "query/result-limit",
            "query result exceeded its row limit",
        ));
    }
    Ok(match &query.find {
        FindSpec::Relation(_) => QueryResult::Relation(output),
        FindSpec::Collection(_) => {
            QueryResult::Collection(output.into_iter().map(|mut row| row.remove(0)).collect())
        }
        FindSpec::Tuple(_) => QueryResult::Tuple(output.into_iter().next()),
        FindSpec::Scalar(_) => {
            QueryResult::Scalar(output.into_iter().next().map(|mut row| row.remove(0)))
        }
    })
}

fn aggregate_rows(
    elements: &[FindElement],
    basis_variables: &[Variable],
    basis: &[Vec<BoundValue>],
    sources: &BTreeMap<&str, &DatabaseValue>,
    pull_budget: &mut QueryPullBudget,
) -> Result<Vec<Vec<QueryValue>>, SemanticError> {
    let group_variables: Vec<_> = elements
        .iter()
        .filter_map(|element| match element {
            FindElement::Variable(variable) | FindElement::Pull { variable, .. } => Some(variable),
            _ => None,
        })
        .collect();
    let mut groups: Vec<(Vec<BoundValue>, Vec<&Vec<BoundValue>>)> = Vec::new();
    for row in basis {
        let key = group_variables
            .iter()
            .map(|variable| {
                row[basis_variables
                    .iter()
                    .position(|candidate| candidate == *variable)
                    .unwrap()]
                .clone()
            })
            .collect::<Vec<_>>();
        if let Some((_, rows)) = groups.iter_mut().find(|(candidate, _)| *candidate == key) {
            rows.push(row);
        } else {
            groups.push((key, vec![row]));
        }
    }
    if groups.is_empty() && group_variables.is_empty() {
        groups.push((Vec::new(), Vec::new()));
    }
    groups
        .into_iter()
        .map(|(_, rows)| {
            elements
                .iter()
                .map(|element| match element {
                    FindElement::Variable(variable) => rows
                        .first()
                        .and_then(|row| {
                            basis_variables
                                .iter()
                                .position(|candidate| candidate == variable)
                                .map(|index| row[index].query_value())
                        })
                        .ok_or_else(|| {
                            fault("query/missing-group-key", "aggregate group key is missing")
                        }),
                    FindElement::Pull {
                        source,
                        variable,
                        pattern,
                    } => {
                        let row = rows.first().ok_or_else(|| {
                            fault("query/missing-group-key", "pull group key is missing")
                        })?;
                        let index = basis_variables
                            .iter()
                            .position(|candidate| candidate == variable)
                            .ok_or_else(|| {
                                fault("query/missing-group-key", "pull variable is missing")
                            })?;
                        let entity = row[index].stored().and_then(entity_id).ok_or_else(|| {
                            SemanticError::incorrect(
                                "query/pull-entity",
                                "pull expression variable must bind an entity id",
                            )
                        })?;
                        find_source(sources, source)?.pull_for_query(pattern, entity, pull_budget)
                    }
                    FindElement::Aggregate { function, variable } => {
                        let index = basis_variables
                            .iter()
                            .position(|candidate| candidate == variable)
                            .ok_or_else(|| {
                                SemanticError::incorrect(
                                    "query/unbound-aggregate-variable",
                                    format!("aggregate variable {} is unbound", variable.name()),
                                )
                            })?;
                        aggregate(*function, rows.iter().map(|row| &row[index]).collect())
                    }
                })
                .collect()
        })
        .collect()
}

fn aggregate(
    function: Aggregate,
    mut values: Vec<&BoundValue>,
) -> Result<QueryValue, SemanticError> {
    match function {
        Aggregate::Count => Ok(QueryValue::Scalar(Value::Long(values.len() as i64))),
        Aggregate::CountDistinct => {
            let mut unique = Vec::new();
            for value in values {
                if !unique.contains(&value) {
                    unique.push(value);
                }
            }
            Ok(QueryValue::Scalar(Value::Long(unique.len() as i64)))
        }
        Aggregate::Min | Aggregate::Max => {
            let value = if function == Aggregate::Min {
                values.into_iter().min_by(|a, b| a.index_cmp(b))
            } else {
                values.into_iter().max_by(|a, b| a.index_cmp(b))
            };
            Ok(value.map_or(QueryValue::Nil, BoundValue::query_value))
        }
        Aggregate::Sum
        | Aggregate::Average
        | Aggregate::Median
        | Aggregate::Variance
        | Aggregate::StandardDeviation => {
            if values.is_empty() && function == Aggregate::Sum {
                return Ok(QueryValue::Scalar(Value::Long(0)));
            }
            if values.is_empty() {
                return Err(SemanticError::incorrect(
                    "query/empty-aggregate",
                    "numeric aggregate has no values",
                ));
            }
            let all_long = values
                .iter()
                .all(|value| matches!(value, BoundValue::Stored(Value::Long(_))));
            if all_long && matches!(function, Aggregate::Sum | Aggregate::Median) {
                if function == Aggregate::Median {
                    values.sort_by(|left, right| left.index_cmp(right));
                    let middle = values.len() / 2;
                    let value = if values.len() % 2 == 1 {
                        let BoundValue::Stored(Value::Long(value)) = values[middle] else {
                            unreachable!()
                        };
                        *value
                    } else {
                        let (
                            BoundValue::Stored(Value::Long(left)),
                            BoundValue::Stored(Value::Long(right)),
                        ) = (values[middle - 1], values[middle])
                        else {
                            unreachable!()
                        };
                        left.checked_add(*right).ok_or_else(|| {
                            SemanticError::incorrect("query/arithmetic", "median sum overflow")
                        })? / 2
                    };
                    return Ok(QueryValue::Scalar(Value::Long(value)));
                }
                let total = values
                    .iter()
                    .try_fold(0_i64, |total, value| {
                        if let BoundValue::Stored(Value::Long(value)) = value {
                            total.checked_add(*value)
                        } else {
                            None
                        }
                    })
                    .ok_or_else(|| {
                        SemanticError::incorrect("query/arithmetic", "aggregate sum overflow")
                    })?;
                Ok(QueryValue::Scalar(Value::Long(total)))
            } else {
                let mut numeric = values
                    .iter()
                    .map(|value| as_f64(require_stored(value, "query/numeric-type")?))
                    .collect::<Result<Vec<_>, _>>()?;
                let total: f64 = numeric.iter().sum();
                let mean = total / numeric.len() as f64;
                let result = match function {
                    Aggregate::Sum => total,
                    Aggregate::Average => mean,
                    Aggregate::Median => {
                        numeric.sort_by(|left, right| left.total_cmp(right));
                        let middle = numeric.len() / 2;
                        if numeric.len() % 2 == 1 {
                            numeric[middle]
                        } else {
                            (numeric[middle - 1] + numeric[middle]) / 2.0
                        }
                    }
                    Aggregate::Variance | Aggregate::StandardDeviation => {
                        let variance = numeric
                            .iter()
                            .map(|value| (value - mean).powi(2))
                            .sum::<f64>()
                            / numeric.len() as f64;
                        if function == Aggregate::StandardDeviation {
                            variance.sqrt()
                        } else {
                            variance
                        }
                    }
                    _ => unreachable!(),
                };
                Ok(QueryValue::Scalar(Value::Double(result)))
            }
        }
        Aggregate::Distinct => {
            values.sort_by(|left, right| left.index_cmp(right));
            values.dedup_by(|left, right| left.index_cmp(right).is_eq());
            Ok(QueryValue::Collection(
                values.into_iter().map(BoundValue::query_value).collect(),
            ))
        }
        Aggregate::MinN(limit) | Aggregate::MaxN(limit) => {
            values.sort_by(|a, b| a.index_cmp(b));
            if matches!(function, Aggregate::MaxN(_)) {
                values.reverse();
            }
            Ok(QueryValue::Collection(
                values
                    .into_iter()
                    .take(limit)
                    .map(BoundValue::query_value)
                    .collect(),
            ))
        }
    }
}

fn validate_or(branches: &[Vec<Clause>], join: Option<&[Variable]>) -> Result<(), SemanticError> {
    if branches.is_empty() {
        return Err(SemanticError::incorrect(
            "query/empty-or",
            "or requires at least one branch",
        ));
    }
    if join.is_none() {
        let expected: BTreeSet<_> = variables_in_clauses(&branches[0]).into_iter().collect();
        if branches[1..].iter().any(|branch| {
            variables_in_clauses(branch)
                .into_iter()
                .collect::<BTreeSet<_>>()
                != expected
        }) {
            return Err(SemanticError::incorrect(
                "query/or-variable-mismatch",
                "or branches must use the same variables",
            ));
        }
    }
    Ok(())
}

fn variables_in_clauses(clauses: &[Clause]) -> Vec<Variable> {
    let mut variables = BTreeSet::new();
    for clause in clauses {
        collect_clause_variables(clause, &mut variables);
    }
    variables.into_iter().collect()
}

fn collect_clause_variables(clause: &Clause, output: &mut BTreeSet<Variable>) {
    let mut terms = Vec::new();
    match clause {
        Clause::Pattern(pattern) => {
            terms.extend([&pattern.entity, &pattern.attribute, &pattern.value]);
            terms.extend(pattern.transaction.iter());
            terms.extend(pattern.added.iter());
        }
        Clause::Predicate { args, .. }
        | Clause::Function { args, .. }
        | Clause::Rule { args, .. } => terms.extend(args),
        Clause::Not { clauses, .. } => {
            for variable in variables_in_clauses(clauses) {
                output.insert(variable);
            }
        }
        Clause::Or { branches, .. } => {
            for branch in branches {
                for variable in variables_in_clauses(branch) {
                    output.insert(variable);
                }
            }
        }
    }
    for term in terms {
        if let Term::Variable(variable) = term {
            output.insert(variable.clone());
        }
    }
    if let Clause::Function { binding, .. } = clause {
        for variable in binding_variables(binding) {
            output.insert(variable.clone());
        }
    }
}

fn binding_variables(binding: &Binding) -> Vec<&Variable> {
    match binding {
        Binding::Scalar(variable) | Binding::Collection(variable) => vec![variable],
        Binding::Tuple(variables) | Binding::Relation(variables) => {
            variables.iter().flatten().collect()
        }
    }
}
fn find_variables(elements: &[FindElement]) -> Vec<Variable> {
    elements
        .iter()
        .map(|element| match element {
            FindElement::Variable(variable)
            | FindElement::Pull { variable, .. }
            | FindElement::Aggregate { variable, .. } => variable.clone(),
        })
        .collect()
}
fn find_elements(find: &FindSpec) -> &[FindElement] {
    match find {
        FindSpec::Relation(elements) | FindSpec::Tuple(elements) => elements,
        FindSpec::Collection(element) | FindSpec::Scalar(element) => std::slice::from_ref(element),
    }
}
fn project_element(
    element: &FindElement,
    basis_variables: &[Variable],
    row: &[BoundValue],
    sources: &BTreeMap<&str, &DatabaseValue>,
    pull_budget: &mut QueryPullBudget,
) -> Result<QueryValue, SemanticError> {
    let variable = match element {
        FindElement::Variable(variable) | FindElement::Aggregate { variable, .. } => variable,
        FindElement::Pull { variable, .. } => variable,
    };
    let index = basis_variables
        .iter()
        .position(|candidate| candidate == variable)
        .ok_or_else(|| {
            fault(
                "query/missing-projection",
                "find variable is missing from projection",
            )
        })?;
    match element {
        FindElement::Pull {
            source, pattern, ..
        } => {
            let entity = row[index].stored().and_then(entity_id).ok_or_else(|| {
                SemanticError::incorrect(
                    "query/pull-entity",
                    "pull expression variable must bind an entity id",
                )
            })?;
            find_source(sources, source)?.pull_for_query(pattern, entity, pull_budget)
        }
        _ => Ok(row[index].query_value()),
    }
}
fn resolve_args(args: &[Term], row: &Row) -> Result<Vec<BoundValue>, SemanticError> {
    args.iter()
        .map(|term| match term {
            Term::Constant(value) => Ok(BoundValue::Stored(value.clone())),
            Term::Nil => Ok(BoundValue::Nil),
            Term::Variable(variable) => row.get(variable).cloned().ok_or_else(|| {
                SemanticError::incorrect(
                    "query/insufficient-binding",
                    format!("{} is not bound", variable.name()),
                )
            }),
            Term::Blank => Err(SemanticError::incorrect(
                "query/blank-expression-arg",
                "blank cannot be an expression argument",
            )),
        })
        .collect()
}
fn term_bound_value(term: &Term, row: &Row) -> Option<BoundValue> {
    match term {
        Term::Constant(value) => Some(BoundValue::Stored(value.clone())),
        Term::Nil => Some(BoundValue::Nil),
        Term::Variable(variable) => row.get(variable).cloned(),
        Term::Blank => None,
    }
}
fn term_is_bound(term: &Term, row: &Row) -> bool {
    !matches!(term, Term::Blank)
        && !matches!(term, Term::Variable(variable) if !row.contains_key(variable))
}
fn entity_id(value: &Value) -> Option<u64> {
    match value {
        Value::Ref(value) => Some(*value),
        Value::Long(value) => u64::try_from(*value).ok(),
        _ => None,
    }
}
fn entity_value(database: &DatabaseValue, value: &Value) -> Result<Option<u64>, SemanticError> {
    match value {
        Value::Keyword(keyword) => database.entid(keyword).map(Some).ok_or_else(|| {
            SemanticError::incorrect(
                "query/unknown-ident",
                format!("unknown ident {}", keyword.qualified_name()),
            )
        }),
        Value::Tuple(parts) => match parts.as_slice() {
            [Some(attribute), Some(value)] => {
                let attribute = attribute_value(database, attribute)?;
                database.lookup(attribute, value)
            }
            _ => Err(SemanticError::incorrect(
                "query/entity-value",
                "lookup ref must contain exactly an attribute and value",
            )),
        },
        _ => Ok(entity_id(value)),
    }
}
fn resolve_entity(
    database: &DatabaseValue,
    term: &Term,
    row: &Row,
) -> Result<Option<u64>, SemanticError> {
    match term_bound_value(term, row) {
        Some(BoundValue::Stored(value)) => entity_value(database, &value),
        Some(BoundValue::Nil) | None => Ok(None),
    }
}
fn resolve_attribute(
    database: &DatabaseValue,
    term: &Term,
    row: &Row,
) -> Result<Option<u32>, SemanticError> {
    match term_bound_value(term, row) {
        Some(BoundValue::Stored(value)) => attribute_value(database, &value).map(Some),
        Some(BoundValue::Nil) | None => Ok(None),
    }
}
fn attribute_value(database: &DatabaseValue, value: &Value) -> Result<u32, SemanticError> {
    let entity = match value {
        Value::Keyword(keyword) => database.entid(keyword).ok_or_else(|| {
            SemanticError::incorrect(
                "query/unknown-attribute",
                format!("unknown attribute {}", keyword.qualified_name()),
            )
        })?,
        Value::Ref(value) => *value,
        Value::Long(value) => u64::try_from(*value).map_err(|_| {
            SemanticError::incorrect("query/attribute-value", "attribute id is out of range")
        })?,
        _ => Err(SemanticError::incorrect(
            "query/attribute-value",
            "attribute must be a keyword or id",
        ))?,
    };
    let attribute = schema_eid_to_attr_id(entity).map_err(|_| {
        SemanticError::incorrect(
            "query/attribute-value",
            format!("entity {entity} cannot identify an installed attribute"),
        )
    })?;
    database.schema().attribute(attribute).map_err(|_| {
        SemanticError::incorrect(
            "query/unknown-attribute",
            format!("unknown attribute id {attribute}"),
        )
    })?;
    Ok(attribute)
}
fn resolve_pattern_value(
    database: &DatabaseValue,
    attribute: Option<u32>,
    value: &Value,
) -> Result<Value, SemanticError> {
    let Some(attribute) = attribute else {
        return Ok(value.clone());
    };
    if database.schema().attribute(attribute)?.value_type == crate::ValueType::Ref {
        return match value {
            Value::Keyword(keyword) => database.entid(keyword).map(Value::Ref).ok_or_else(|| {
                SemanticError::incorrect(
                    "query/unknown-ident",
                    format!("unknown ident {}", keyword.qualified_name()),
                )
            }),
            Value::Long(entity) => u64::try_from(*entity).map(Value::Ref).map_err(|_| {
                SemanticError::incorrect(
                    "query/entity-value",
                    "ref value entity id is out of range",
                )
            }),
            _ => Ok(value.clone()),
        };
    }
    Ok(value.clone())
}
fn unify_entity_term(
    database: &DatabaseValue,
    row: &mut Row,
    term: &Term,
    resolved: Option<u64>,
    entity: u64,
) -> Result<bool, SemanticError> {
    match term {
        Term::Blank => Ok(true),
        Term::Nil => Ok(false),
        Term::Constant(_) => Ok(resolved == Some(entity)),
        Term::Variable(variable) => match row.get(variable) {
            Some(BoundValue::Nil) => Ok(false),
            Some(BoundValue::Stored(expected)) => Ok(match resolved {
                Some(resolved) => resolved == entity,
                None => entity_value(database, expected)? == Some(entity),
            }),
            None => {
                row.insert(variable.clone(), BoundValue::Stored(Value::Ref(entity)));
                Ok(true)
            }
        },
    }
}
fn unify_attribute_term(
    database: &DatabaseValue,
    row: &mut Row,
    term: &Term,
    resolved: Option<u32>,
    attribute: u32,
) -> Result<bool, SemanticError> {
    match term {
        Term::Blank => Ok(true),
        Term::Nil => Ok(false),
        Term::Constant(_) => Ok(resolved == Some(attribute)),
        Term::Variable(variable) => match row.get(variable) {
            Some(BoundValue::Nil) => Ok(false),
            Some(BoundValue::Stored(expected)) => Ok(match resolved {
                Some(resolved) => resolved == attribute,
                None => attribute_value(database, expected)? == attribute,
            }),
            None => {
                row.insert(
                    variable.clone(),
                    BoundValue::Stored(Value::Ref(u64::from(attribute))),
                );
                Ok(true)
            }
        },
    }
}
fn unify_value_term(
    database: &DatabaseValue,
    row: &mut Row,
    term: &Term,
    resolved_attribute: Option<u32>,
    resolved: Option<&Value>,
    value: &Value,
) -> Result<bool, SemanticError> {
    match term {
        Term::Blank => Ok(true),
        Term::Nil => Ok(false),
        Term::Constant(_) => Ok(resolved == Some(value)),
        Term::Variable(variable) => match row.get(variable) {
            Some(BoundValue::Nil) => Ok(false),
            Some(BoundValue::Stored(expected)) => Ok(match resolved {
                Some(resolved) => resolved == value,
                None => resolve_pattern_value(database, resolved_attribute, expected)? == *value,
            }),
            None => {
                row.insert(variable.clone(), BoundValue::Stored(value.clone()));
                Ok(true)
            }
        },
    }
}
fn unify_term(row: &mut Row, term: &Term, value: &BoundValue) -> bool {
    match term {
        Term::Blank => true,
        Term::Nil => matches!(value, BoundValue::Nil),
        Term::Constant(expected) => {
            matches!(value, BoundValue::Stored(actual) if expected == actual)
        }
        Term::Variable(variable) => unify_variable(row, variable, value),
    }
}
fn unify_variable(row: &mut Row, variable: &Variable, value: &BoundValue) -> bool {
    match row.get(variable) {
        Some(expected) => expected == value,
        None => {
            row.insert(variable.clone(), value.clone());
            true
        }
    }
}
fn push_unique_row(rows: &mut Vec<Row>, row: Row) {
    if !rows.contains(&row) {
        rows.push(row);
    }
}
fn project_row(row: &Row, variables: &[Variable]) -> Row {
    variables
        .iter()
        .filter_map(|variable| {
            row.get(variable)
                .cloned()
                .map(|value| (variable.clone(), value))
        })
        .collect()
}
fn dedupe_rows(rows: Vec<Row>) -> Vec<Row> {
    let mut result = Vec::new();
    for row in rows {
        push_unique_row(&mut result, row);
    }
    result
}
fn dedupe_query_rows(rows: &mut Vec<Vec<QueryValue>>, dedupe: bool) {
    if dedupe {
        let mut unique = Vec::new();
        for row in rows.drain(..) {
            if !unique.contains(&row) {
                unique.push(row);
            }
        }
        *rows = unique;
    }
}
fn source_database<'a>(
    state: &'a State<'_>,
    source: &str,
) -> Result<&'a DatabaseValue, SemanticError> {
    state.sources.get(source).copied().ok_or_else(|| {
        SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
    })
}

fn find_source<'a>(
    sources: &'a BTreeMap<&str, &DatabaseValue>,
    source: &str,
) -> Result<&'a DatabaseValue, SemanticError> {
    sources.get(source).copied().ok_or_else(|| {
        SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
    })
}
fn effective_source<'a>(source: &'a str, inherited_source: Option<&'a str>) -> &'a str {
    if source == "$" {
        inherited_source.unwrap_or(source)
    } else {
        source
    }
}
fn as_f64(value: &Value) -> Result<f64, SemanticError> {
    match value {
        Value::Long(value) => Ok(*value as f64),
        Value::Float(value) => Ok(f64::from(*value)),
        Value::Double(value) => Ok(*value),
        _ => Err(SemanticError::incorrect(
            "query/numeric-type",
            "numeric operation requires long, float, or double",
        )),
    }
}
fn result_len(result: &QueryResult) -> usize {
    match result {
        QueryResult::Relation(rows) => rows.len(),
        QueryResult::Collection(values) => values.len(),
        QueryResult::Tuple(value) => usize::from(value.is_some()),
        QueryResult::Scalar(value) => usize::from(value.is_some()),
    }
}
fn clause_name(clause: &Clause) -> &'static str {
    match clause {
        Clause::Pattern(_) => "pattern",
        Clause::Predicate { .. } => "predicate",
        Clause::Function { .. } => "function",
        Clause::Not { .. } => "not",
        Clause::Or { .. } => "or",
        Clause::Rule { .. } => "rule",
    }
}
fn resource(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Busy, code, message)
}
fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

impl State<'_> {
    fn check(&mut self, amount: usize) -> Result<(), SemanticError> {
        self.work = self.work.saturating_add(amount);
        if self.control.cancel.load(AtomicOrdering::Relaxed) {
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
        if self.work > self.control.max_work {
            return Err(resource(
                "query/work-limit",
                "query exceeded its work limit",
            ));
        }
        Ok(())
    }
}
