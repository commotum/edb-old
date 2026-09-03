use crate::{
    Database, ErrorCategory, IndexOrder, IndexPrefix, Keyword, Program, ProgramControl,
    ProgramHash, ProgramKind, ProgramOutput, ProgramRuntime, PullPattern, SemanticError, Value,
    View,
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
pub struct QuerySource<'a> {
    pub name: String,
    pub database: &'a Database,
    pub view: View,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum QueryValue {
    Scalar(Value),
    Collection(Vec<QueryValue>),
    Tuple(Vec<QueryValue>),
    Map(Vec<(Keyword, QueryValue)>),
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

type NativeQueryFunction = dyn Fn(&Database, &[Value], &QueryControl) -> Result<Vec<Vec<Value>>, SemanticError>
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
        F: Fn(&Database, &[Value], &QueryControl) -> Result<Vec<Vec<Value>>, SemanticError>
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
                        max_output: control.max_result_rows,
                        cancelled: Some(control.cancel.as_ref()),
                    };
                    match ProgramRuntime.execute(&program, database, arguments, program_control)? {
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
        database: &Database,
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

type Row = BTreeMap<Variable, Value>;
type RuleRelations = BTreeMap<String, Vec<Vec<Value>>>;

struct State<'a> {
    sources: BTreeMap<&'a str, (&'a Database, View)>,
    control: &'a QueryControl,
    deadline: Option<Instant>,
    work: usize,
    stats: QueryStats,
    plan: Vec<PlanStep>,
    extensions: Option<&'a QueryExtensions>,
}

impl QueryEngine {
    pub fn execute(
        query: &Query,
        sources: &[QuerySource<'_>],
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        Self::execute_with_extensions(query, sources, inputs, control, None)
    }

    pub fn execute_with_extensions(
        query: &Query,
        sources: &[QuerySource<'_>],
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: Option<&QueryExtensions>,
    ) -> Result<QueryOutcome, SemanticError> {
        validate_query(query, inputs)?;
        let mut source_map = BTreeMap::new();
        for source in sources {
            if source_map
                .insert(source.name.as_str(), (source.database, source.view))
                .is_some()
            {
                return Err(SemanticError::incorrect(
                    "query/duplicate-source",
                    format!("source {} is supplied more than once", source.name),
                ));
            }
        }
        if !source_map.contains_key("$") {
            return Err(SemanticError::incorrect(
                "query/missing-default-source",
                "query needs a source named $",
            ));
        }
        let deadline = control.timeout.map(|timeout| Instant::now() + timeout);
        let mut state = State {
            sources: source_map,
            control,
            deadline,
            work: 0,
            stats: QueryStats::default(),
            plan: Vec::new(),
            extensions,
        };
        let initial = bind_inputs(&query.inputs, inputs)?;
        let rules = build_rule_relations(&query.rules, &mut state)?;
        let rows = evaluate_clauses(&query.clauses, initial, &rules, &mut state)?;
        let default_database = state.sources["$"].0;
        let result = shape_results(query, rows, control.max_result_rows, default_database)?;
        state.stats.rows_produced = result_len(&result) as u64;
        Ok(QueryOutcome {
            result,
            stats: state.stats,
            plan: state.plan,
        })
    }
}

impl Database {
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
                database: self,
                view: View::Current,
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
                database: self,
                view: View::Current,
            }],
            inputs,
            control,
            Some(extensions),
        )
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
    let mut arities = BTreeMap::new();
    for rule in &query.rules {
        if arities
            .insert(rule.name.as_str(), rule.head.len())
            .is_some_and(|arity| arity != rule.head.len())
        {
            return Err(SemanticError::incorrect(
                "query/rule-arity",
                format!("rule {} has inconsistent arity", rule.name),
            ));
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
                        && !unify_variable(&mut candidate, variable, value)
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
    rules: &RuleRelations,
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
        let (next, access) = evaluate_clause(clause, rows, rules, state)?;
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
    rules: &RuleRelations,
    state: &mut State<'_>,
) -> Result<(Vec<Row>, String), SemanticError> {
    match clause {
        Clause::Pattern(pattern) => evaluate_pattern(pattern, rows, state),
        Clause::Predicate {
            predicate,
            source,
            args,
        } => {
            let mut next = Vec::new();
            for row in rows {
                state.check(1)?;
                let values = resolve_args(args, &row)?;
                if evaluate_predicate(*predicate, source, &values, state)? {
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
                for produced in evaluate_function(function.clone(), source, &values, state)? {
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
                if evaluate_clauses(clauses, vec![seed], rules, state)?.is_empty() {
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
                    for produced in evaluate_clauses(branch, vec![seed], rules, state)? {
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
        Clause::Rule { name, args, .. } => {
            let relation = rules.get(name).ok_or_else(|| {
                SemanticError::incorrect("query/unknown-rule", format!("unknown rule {name}"))
            })?;
            let mut next = Vec::new();
            for row in rows {
                for tuple in relation {
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
    state: &mut State<'_>,
) -> Result<(Vec<Row>, String), SemanticError> {
    let (database, view) = state
        .sources
        .get(pattern.source.as_str())
        .copied()
        .ok_or_else(|| {
            SemanticError::incorrect(
                "query/unknown-source",
                format!("unknown source {}", pattern.source),
            )
        })?;
    let mut next = Vec::new();
    let mut access = "EAVT scan".to_owned();
    for row in rows {
        state.check(1)?;
        let entity = term_value(&pattern.entity, &row).and_then(entity_id);
        let attribute = resolve_attribute(database, &pattern.attribute, &row)?;
        let value = term_value(&pattern.value, &row);
        let (datoms, selected) = select_datoms(
            database,
            view,
            entity,
            attribute,
            value,
            state.control.force_scan,
        )?;
        access = selected;
        if access != "EAVT scan" {
            state.stats.index_seeks += 1;
        }
        state.stats.datoms_examined += datoms.len() as u64;
        for datom in datoms {
            state.check(1)?;
            let mut candidate = row.clone();
            let attribute_value =
                attribute_term_value(database, &pattern.attribute, &candidate, datom.attribute)?;
            if unify_term(&mut candidate, &pattern.entity, &Value::Ref(datom.entity))
                && unify_term(&mut candidate, &pattern.attribute, &attribute_value)
                && unify_term(&mut candidate, &pattern.value, &datom.value)
                && pattern
                    .transaction
                    .as_ref()
                    .is_none_or(|term| unify_term(&mut candidate, term, &Value::Ref(datom.tx)))
                && pattern
                    .added
                    .as_ref()
                    .is_none_or(|term| unify_term(&mut candidate, term, &Value::Bool(datom.added)))
            {
                push_unique_row(&mut next, candidate);
            }
        }
    }
    Ok((next, access))
}

fn select_datoms(
    database: &Database,
    view: View,
    entity: Option<u64>,
    attribute: Option<u32>,
    value: Option<&Value>,
    force_scan: bool,
) -> Result<(Vec<crate::Datom>, String), SemanticError> {
    if view == View::Current && !force_scan {
        if let Some(entity) = entity {
            let prefix = IndexPrefix::Eavt {
                entity,
                attribute,
                value: attribute.and(value).cloned(),
            };
            return Ok((
                database.datoms_with_prefix(&prefix)?.to_vec(),
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
                    database.datoms_with_prefix(&prefix)?.to_vec(),
                    "AVET seek".into(),
                ));
            }
            let prefix = IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            };
            return Ok((
                database.datoms_with_prefix(&prefix)?.to_vec(),
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
                database.datoms_with_prefix(&prefix)?.to_vec(),
                "VAET seek".into(),
            ));
        }
    }
    Ok((database.datoms(view, IndexOrder::Eavt), "EAVT scan".into()))
}

fn clause_ready(clause: &Clause, row: &Row, rules: &RuleRelations) -> bool {
    match clause {
        Clause::Pattern(_) | Clause::Or { .. } => true,
        Clause::Predicate { args, .. } | Clause::Function { args, .. } => args.iter().all(|term| !matches!(term, Term::Variable(variable) if !row.contains_key(variable))),
        Clause::Not { join, clauses } => join.clone().unwrap_or_else(|| variables_in_clauses(clauses)).iter().all(|variable| row.contains_key(variable)),
        Clause::Rule { name, args, .. } => rules.get(&format!("@required:{name}")).is_none_or(|required| required[0].iter().enumerate().filter(|(_, value)| **value == Value::Bool(true)).all(|(index, _)| !matches!(&args[index], Term::Variable(variable) if !row.contains_key(variable)))),
    }
}

fn clause_score(clause: &Clause, row: &Row) -> usize {
    match clause {
        Clause::Pattern(pattern) => {
            [&pattern.entity, &pattern.attribute, &pattern.value]
                .into_iter()
                .filter(|term| term_value(term, row).is_some())
                .count()
                * 10
        }
        Clause::Predicate { .. } | Clause::Not { .. } => 40,
        Clause::Function { .. } => 30,
        Clause::Rule { .. } => 20,
        Clause::Or { .. } => 10,
    }
}

fn build_rule_relations(
    rules: &[Rule],
    state: &mut State<'_>,
) -> Result<RuleRelations, SemanticError> {
    let mut relations = RuleRelations::new();
    for rule in rules {
        relations.entry(rule.name.clone()).or_default();
        let required = relations
            .entry(format!("@required:{}", rule.name))
            .or_default();
        if required.is_empty() {
            required.push(
                (0..rule.head.len())
                    .map(|index| Value::Bool(rule.required.contains(&index)))
                    .collect(),
            );
        }
    }
    for iteration in 0..128 {
        let before: usize = relations
            .iter()
            .filter(|(name, _)| !name.starts_with('@'))
            .map(|(_, rows)| rows.len())
            .sum();
        for rule in rules {
            let rows =
                evaluate_clauses(&rule.clauses, vec![Row::new()], &relations.clone(), state)?;
            let relation = relations
                .get_mut(&rule.name)
                .expect("rule relation initialized");
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
                if !relation.contains(&tuple) {
                    relation.push(tuple);
                }
            }
        }
        state.stats.rule_iterations += 1;
        let after: usize = relations
            .iter()
            .filter(|(name, _)| !name.starts_with('@'))
            .map(|(_, rows)| rows.len())
            .sum();
        if after == before {
            return Ok(relations);
        }
        state.check(after - before)?;
        if iteration == 127 {
            return Err(resource(
                "query/rule-iteration-limit",
                "rules did not reach a fixed point",
            ));
        }
    }
    unreachable!()
}

fn evaluate_predicate(
    predicate: Predicate,
    source: &str,
    args: &[Value],
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
            let (database, _) = state.sources.get(source).copied().ok_or_else(|| {
                SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
            })?;
            let entity = entity_id(&args[0]).ok_or_else(|| {
                SemanticError::incorrect("query/entity-value", "missing entity must be a ref")
            })?;
            let attribute = attribute_value(database, &args[1])?;
            Ok(database.values(entity, attribute).is_empty())
        }
    }
}

fn evaluate_function(
    function: Function,
    source: &str,
    args: &[Value],
    state: &State<'_>,
) -> Result<Vec<Vec<Value>>, SemanticError> {
    match function {
        Function::Ground => Ok(vec![args.to_vec()]),
        Function::Tuple => Ok(vec![vec![Value::Tuple(
            args.iter().cloned().map(Some).collect(),
        )]]),
        Function::Untuple => match args {
            [Value::Tuple(values)] => Ok(vec![
                values
                    .iter()
                    .map(|value| value.clone().unwrap_or(Value::String(String::new())))
                    .collect(),
            ]),
            _ => Err(SemanticError::incorrect(
                "query/function-type",
                "untuple requires one tuple",
            )),
        },
        Function::Add | Function::Subtract | Function::Multiply | Function::Divide => {
            numeric_function(function, args).map(|value| vec![vec![value]])
        }
        Function::GetElse => {
            if args.len() != 3 {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "get-else requires entity, attribute, and default",
                ));
            }
            let database = source_database(state, source)?;
            let entity = entity_id(&args[0]).ok_or_else(|| {
                SemanticError::incorrect("query/entity-value", "get-else entity must be a ref")
            })?;
            let attribute = attribute_value(database, &args[1])?;
            if database.schema().attribute(attribute)?.cardinality != crate::Cardinality::One {
                return Err(SemanticError::incorrect(
                    "query/get-else-cardinality",
                    "get-else requires cardinality one",
                ));
            }
            Ok(vec![vec![
                database
                    .values(entity, attribute)
                    .first()
                    .cloned()
                    .cloned()
                    .unwrap_or_else(|| args[2].clone()),
            ]])
        }
        Function::GetSome => {
            if args.len() < 2 {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "get-some requires entity and attributes",
                ));
            }
            let database = source_database(state, source)?;
            let entity = entity_id(&args[0]).ok_or_else(|| {
                SemanticError::incorrect("query/entity-value", "get-some entity must be a ref")
            })?;
            for value in &args[1..] {
                let attribute = attribute_value(database, value)?;
                if let Some(found) = database.values(entity, attribute).first() {
                    return Ok(vec![vec![
                        Value::Keyword(database.schema().attribute(attribute)?.ident.clone()),
                        (*found).clone(),
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
            let rows = extensions.invoke(&name, database, args, state.control)?;
            if rows.len() > state.control.max_result_rows
                || rows.iter().map(Vec::len).sum::<usize>() > state.control.max_intermediate_rows
            {
                return Err(resource(
                    "query/extension-output-limit",
                    "query extension exceeded its output limits",
                ));
            }
            Ok(rows)
        }
    }
}

fn numeric_function(function: Function, args: &[Value]) -> Result<Value, SemanticError> {
    if args.len() != 2 {
        return Err(SemanticError::incorrect(
            "query/function-arity",
            "arithmetic functions require two arguments",
        ));
    }
    match (&args[0], &args[1]) {
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
            let left = as_f64(&args[0])?;
            let right = as_f64(&args[1])?;
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

fn bind_output(row: &Row, binding: &Binding, output: &[Value]) -> Result<Vec<Row>, SemanticError> {
    if let Binding::Collection(variable) = binding {
        let [Value::Tuple(values)] = output else {
            return Err(SemanticError::incorrect(
                "query/function-binding",
                "collection binding requires one tuple value",
            ));
        };
        let mut rows = Vec::new();
        for value in values {
            let value = value.as_ref().ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Unsupported,
                    "query/nil-tuple-slot",
                    "nil tuple slots are not supported as query bindings",
                )
            })?;
            let mut candidate = row.clone();
            if unify_variable(&mut candidate, variable, value) {
                push_unique_row(&mut rows, candidate);
            }
        }
        return Ok(rows);
    }
    let bindings: Vec<(Option<&Variable>, &Value)> = match binding {
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
    database: &Database,
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
        aggregate_rows(elements, &basis_variables, &basis, database)?
    } else {
        basis
            .into_iter()
            .map(|row| {
                elements
                    .iter()
                    .map(|element| project_element(element, &basis_variables, &row, database))
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
    basis: &[Vec<Value>],
    database: &Database,
) -> Result<Vec<Vec<QueryValue>>, SemanticError> {
    let group_variables: Vec<_> = elements
        .iter()
        .filter_map(|element| match element {
            FindElement::Variable(variable) | FindElement::Pull { variable, .. } => Some(variable),
            _ => None,
        })
        .collect();
    let mut groups: Vec<(Vec<Value>, Vec<&Vec<Value>>)> = Vec::new();
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
                                .map(|index| QueryValue::Scalar(row[index].clone()))
                        })
                        .ok_or_else(|| {
                            fault("query/missing-group-key", "aggregate group key is missing")
                        }),
                    FindElement::Pull { variable, pattern } => {
                        let row = rows.first().ok_or_else(|| {
                            fault("query/missing-group-key", "pull group key is missing")
                        })?;
                        let index = basis_variables
                            .iter()
                            .position(|candidate| candidate == variable)
                            .ok_or_else(|| {
                                fault("query/missing-group-key", "pull variable is missing")
                            })?;
                        let entity = entity_id(&row[index]).ok_or_else(|| {
                            SemanticError::incorrect(
                                "query/pull-entity",
                                "pull expression variable must bind an entity id",
                            )
                        })?;
                        database.pull(pattern, entity)
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

fn aggregate(function: Aggregate, mut values: Vec<&Value>) -> Result<QueryValue, SemanticError> {
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
            }
            .ok_or_else(|| {
                SemanticError::incorrect("query/empty-aggregate", "min/max has no values")
            })?;
            Ok(QueryValue::Scalar(value.clone()))
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
            let all_long = values.iter().all(|value| matches!(value, Value::Long(_)));
            if all_long && matches!(function, Aggregate::Sum | Aggregate::Median) {
                if function == Aggregate::Median {
                    values.sort_by(|left, right| left.index_cmp(right));
                    let middle = values.len() / 2;
                    let value = if values.len() % 2 == 1 {
                        let Value::Long(value) = values[middle] else {
                            unreachable!()
                        };
                        *value
                    } else {
                        let (Value::Long(left), Value::Long(right)) =
                            (values[middle - 1], values[middle])
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
                        if let Value::Long(value) = value {
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
                    .map(|value| as_f64(value))
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
                values
                    .into_iter()
                    .cloned()
                    .map(QueryValue::Scalar)
                    .collect(),
            ))
        }
        Aggregate::MinN(limit) | Aggregate::MaxN(limit) => {
            values.sort_by(|a, b| a.index_cmp(b));
            if matches!(function, Aggregate::MaxN(_)) {
                values.reverse();
            }
            values.dedup_by(|a, b| a.index_cmp(b).is_eq());
            Ok(QueryValue::Collection(
                values
                    .into_iter()
                    .take(limit)
                    .cloned()
                    .map(QueryValue::Scalar)
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
    row: &[Value],
    database: &Database,
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
        FindElement::Pull { pattern, .. } => {
            let entity = entity_id(&row[index]).ok_or_else(|| {
                SemanticError::incorrect(
                    "query/pull-entity",
                    "pull expression variable must bind an entity id",
                )
            })?;
            database.pull(pattern, entity)
        }
        _ => Ok(QueryValue::Scalar(row[index].clone())),
    }
}
fn resolve_args(args: &[Term], row: &Row) -> Result<Vec<Value>, SemanticError> {
    args.iter()
        .map(|term| match term {
            Term::Constant(value) => Ok(value.clone()),
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
fn term_value<'a>(term: &'a Term, row: &'a Row) -> Option<&'a Value> {
    match term {
        Term::Constant(value) => Some(value),
        Term::Variable(variable) => row.get(variable),
        Term::Blank => None,
    }
}
fn entity_id(value: &Value) -> Option<u64> {
    match value {
        Value::Ref(value) => Some(*value),
        Value::Long(value) => u64::try_from(*value).ok(),
        _ => None,
    }
}
fn resolve_attribute(
    database: &Database,
    term: &Term,
    row: &Row,
) -> Result<Option<u32>, SemanticError> {
    term_value(term, row)
        .map(|value| attribute_value(database, value))
        .transpose()
}
fn attribute_term_value(
    database: &Database,
    term: &Term,
    row: &Row,
    attribute: u32,
) -> Result<Value, SemanticError> {
    Ok(match term_value(term, row) {
        Some(Value::Keyword(_)) => {
            Value::Keyword(database.schema().attribute(attribute)?.ident.clone())
        }
        Some(Value::Long(_)) => Value::Long(i64::from(attribute)),
        _ => Value::Ref(u64::from(attribute)),
    })
}
fn attribute_value(database: &Database, value: &Value) -> Result<u32, SemanticError> {
    match value {
        Value::Keyword(keyword) => database.schema().resolve_ident(keyword).ok_or_else(|| {
            SemanticError::incorrect(
                "query/unknown-attribute",
                format!("unknown attribute {}", keyword.qualified_name()),
            )
        }),
        Value::Ref(value) => u32::try_from(*value).map_err(|_| {
            SemanticError::incorrect("query/attribute-value", "attribute id exceeds u32")
        }),
        Value::Long(value) => u32::try_from(*value).map_err(|_| {
            SemanticError::incorrect("query/attribute-value", "attribute id is out of range")
        }),
        _ => Err(SemanticError::incorrect(
            "query/attribute-value",
            "attribute must be a keyword or id",
        )),
    }
}
fn unify_term(row: &mut Row, term: &Term, value: &Value) -> bool {
    match term {
        Term::Blank => true,
        Term::Constant(expected) => expected == value,
        Term::Variable(variable) => unify_variable(row, variable, value),
    }
}
fn unify_variable(row: &mut Row, variable: &Variable, value: &Value) -> bool {
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
fn source_database<'a>(state: &'a State<'_>, source: &str) -> Result<&'a Database, SemanticError> {
    state
        .sources
        .get(source)
        .map(|(database, _)| *database)
        .ok_or_else(|| {
            SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
        })
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
