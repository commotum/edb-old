use crate::pull::QueryPullBudget;
#[path = "query_numeric.rs"]
mod numeric;
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

#[path = "query_sequence.rs"]
mod sequence;
pub use sequence::QuerySequence;
#[path = "query_nested.rs"]
mod nested;
#[path = "query_sources.rs"]
mod sources;
use sources::SourceRef;
pub use sources::{QueryDataSource, QuerySourceValue};
#[path = "query_prepare.rs"]
mod prepare;
pub use prepare::{PreparedQuery, PreparedQueryCache, PreparedQueryCacheStats};
#[path = "query_dependencies.rs"]
mod dependencies;
use dependencies::EvaluationResult;
#[path = "query_fulltext.rs"]
mod fulltext;
#[path = "query_join.rs"]
mod join;
#[path = "query_ranges.rs"]
mod ranges;

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
    /// Transaction entities in the named immutable log's [start, end) range.
    TxIds,
    /// E/A/V/T/assertion tuples for one transaction in the named immutable log.
    TxData,
    /// Ranked entity/value/transaction/score hits from an eventually consistent
    /// search index, checked against the clause's supplied database view.
    Fulltext,
    Extension(String),
    /// Execute a native subquery against this clause's exact source (as `$`)
    /// and the enclosing named sources, sharing work, deadline and cancellation.
    Query(Box<Query>),
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
    /// Select exactly n items with replacement (empty input produces none).
    Rand(usize),
    /// Select up to n logically distinct items without replacement.
    Sample(usize),
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
        enum Task<'a> {
            Values(&'a QueryValue, &'a QueryValue),
            Length(usize, usize),
        }
        let rank = |value: &Self| match value {
            Self::Nil => 0_u8,
            Self::Scalar(_) => 1,
            Self::Tuple(_) => 2,
            Self::Collection(_) => 3,
            Self::Map(_) => 4,
        };
        let mut pending = vec![Task::Values(self, other)];
        while let Some(task) = pending.pop() {
            let (left, right) = match task {
                Task::Length(left, right) => {
                    let order = left.cmp(&right);
                    if order != Ordering::Equal {
                        return order;
                    }
                    continue;
                }
                Task::Values(left, right) => (left, right),
            };
            let order = rank(left).cmp(&rank(right));
            if order != Ordering::Equal {
                return order;
            }
            match (left, right) {
                (Self::Nil, Self::Nil) => {}
                (Self::Scalar(left), Self::Scalar(right)) => {
                    let order = left.index_cmp(right);
                    if order != Ordering::Equal {
                        return order;
                    }
                }
                (Self::Tuple(left), Self::Tuple(right))
                | (Self::Collection(left), Self::Collection(right)) => {
                    pending.push(Task::Length(left.len(), right.len()));
                    for (left, right) in left.iter().zip(right).rev() {
                        pending.push(Task::Values(left, right));
                    }
                }
                (Self::Map(left), Self::Map(right)) => {
                    let order = compare_query_maps(left, right);
                    if order != Ordering::Equal {
                        return order;
                    }
                }
                _ => unreachable!("equal query-value ranks must have matching variants"),
            }
        }
        Ordering::Equal
    }

    /// Move a result container out while retaining stack-safe destruction of
    /// any other value. Borrowed pattern matching remains available as usual.
    pub fn into_map(mut self) -> Option<Vec<(Self, Self)>> {
        if let Self::Map(values) = &mut self {
            Some(std::mem::take(values))
        } else {
            None
        }
    }
    pub fn into_collection(mut self) -> Option<Vec<Self>> {
        if let Self::Collection(values) = &mut self {
            Some(std::mem::take(values))
        } else {
            None
        }
    }
    pub fn into_tuple(mut self) -> Option<Vec<Self>> {
        if let Self::Tuple(values) = &mut self {
            Some(std::mem::take(values))
        } else {
            None
        }
    }
    pub fn into_scalar(mut self) -> Option<Value> {
        if let Self::Scalar(value) = &mut self {
            Some(std::mem::replace(value, Value::Bool(false)))
        } else {
            None
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

impl Clone for QueryValue {
    fn clone(&self) -> Self {
        enum Task<'a> {
            Value(&'a QueryValue),
            Collection(usize),
            Tuple(usize),
            Map(usize),
        }
        let mut pending = vec![Task::Value(self)];
        let mut values = Vec::new();
        while let Some(task) = pending.pop() {
            match task {
                Task::Value(Self::Nil) => values.push(Self::Nil),
                Task::Value(Self::Scalar(value)) => values.push(Self::Scalar(value.clone())),
                Task::Value(Self::Collection(children)) | Task::Value(Self::Tuple(children)) => {
                    pending.push(if matches!(task, Task::Value(Self::Collection(_))) {
                        Task::Collection(children.len())
                    } else {
                        Task::Tuple(children.len())
                    });
                    pending.extend(children.iter().rev().map(Task::Value));
                }
                Task::Value(Self::Map(entries)) => {
                    pending.push(Task::Map(entries.len()));
                    for (key, value) in entries.iter().rev() {
                        pending.push(Task::Value(value));
                        pending.push(Task::Value(key));
                    }
                }
                Task::Collection(count) => {
                    let children = values.split_off(values.len() - count);
                    values.push(Self::Collection(children));
                }
                Task::Tuple(count) => {
                    let children = values.split_off(values.len() - count);
                    values.push(Self::Tuple(children));
                }
                Task::Map(count) => {
                    let mut children = values.split_off(values.len() - 2 * count).into_iter();
                    let entries = (0..count)
                        .map(|_| (children.next().unwrap(), children.next().unwrap()))
                        .collect();
                    values.push(Self::Map(entries));
                }
            }
        }
        values.pop().expect("one cloned result")
    }
}

impl Drop for QueryValue {
    fn drop(&mut self) {
        fn drain(value: &mut QueryValue, pending: &mut Vec<QueryValue>) {
            match value {
                QueryValue::Collection(values) | QueryValue::Tuple(values) => {
                    pending.append(values)
                }
                QueryValue::Map(entries) => {
                    for (key, value) in std::mem::take(entries) {
                        pending.push(key);
                        pending.push(value);
                    }
                }
                _ => {}
            }
        }
        let mut pending = Vec::new();
        drain(self, &mut pending);
        while let Some(mut value) = pending.pop() {
            drain(&mut value, &mut pending);
        }
    }
}

impl PartialEq for QueryValue {
    fn eq(&self, other: &Self) -> bool {
        self.canonical_cmp(other).is_eq()
    }
}

impl Eq for QueryValue {}

/// Map sorting must not recursively invoke `QueryValue::canonical_cmp` on
/// map-shaped keys or equal-key values. Build canonical child order bottom-up
/// in a flat arena, then compare arena nodes with explicit heap tasks. The
/// arena borrows scalar leaves and never clones or recursively owns results.
enum CanonicalQueryNode<'a> {
    Nil,
    Scalar(&'a Value),
    Tuple(Vec<usize>),
    Collection(Vec<usize>),
    Map(Vec<(usize, usize)>),
}

fn compare_query_maps(
    left: &[(QueryValue, QueryValue)],
    right: &[(QueryValue, QueryValue)],
) -> std::cmp::Ordering {
    if left.is_empty() || right.is_empty() {
        return left.len().cmp(&right.len());
    }
    let mut arena = Vec::new();
    let left = canonical_query_map_root(left, &mut arena);
    let right = canonical_query_map_root(right, &mut arena);
    compare_canonical_query_nodes(&arena, left, right)
}

fn canonical_query_map_root<'a>(
    entries: &'a [(QueryValue, QueryValue)],
    arena: &mut Vec<CanonicalQueryNode<'a>>,
) -> usize {
    enum Task<'a> {
        Value(&'a QueryValue),
        Tuple(usize),
        Collection(usize),
        Map(usize),
    }
    let mut pending = vec![Task::Map(entries.len())];
    for (key, value) in entries.iter().rev() {
        pending.push(Task::Value(value));
        pending.push(Task::Value(key));
    }
    let mut completed = Vec::new();
    while let Some(task) = pending.pop() {
        let node = match task {
            Task::Value(QueryValue::Nil) => CanonicalQueryNode::Nil,
            Task::Value(QueryValue::Scalar(value)) => CanonicalQueryNode::Scalar(value),
            Task::Value(QueryValue::Tuple(values)) => {
                pending.push(Task::Tuple(values.len()));
                pending.extend(values.iter().rev().map(Task::Value));
                continue;
            }
            Task::Value(QueryValue::Collection(values)) => {
                pending.push(Task::Collection(values.len()));
                pending.extend(values.iter().rev().map(Task::Value));
                continue;
            }
            Task::Value(QueryValue::Map(entries)) => {
                pending.push(Task::Map(entries.len()));
                for (key, value) in entries.iter().rev() {
                    pending.push(Task::Value(value));
                    pending.push(Task::Value(key));
                }
                continue;
            }
            Task::Tuple(count) => {
                CanonicalQueryNode::Tuple(completed.split_off(completed.len() - count))
            }
            Task::Collection(count) => {
                CanonicalQueryNode::Collection(completed.split_off(completed.len() - count))
            }
            Task::Map(count) => {
                let children = completed.split_off(completed.len() - 2 * count);
                let mut entries = children
                    .chunks_exact(2)
                    .map(|entry| (entry[0], entry[1]))
                    .collect::<Vec<_>>();
                entries.sort_by(|(left_key, left_value), (right_key, right_value)| {
                    compare_canonical_query_nodes(arena, *left_key, *right_key).then_with(|| {
                        compare_canonical_query_nodes(arena, *left_value, *right_value)
                    })
                });
                CanonicalQueryNode::Map(entries)
            }
        };
        completed.push(arena.len());
        arena.push(node);
    }
    completed.pop().expect("one canonical map root")
}

fn compare_canonical_query_nodes(
    arena: &[CanonicalQueryNode<'_>],
    left: usize,
    right: usize,
) -> std::cmp::Ordering {
    use std::cmp::Ordering;
    enum Task {
        Nodes(usize, usize),
        Length(usize, usize),
    }
    let rank = |node: &CanonicalQueryNode<'_>| match node {
        CanonicalQueryNode::Nil => 0_u8,
        CanonicalQueryNode::Scalar(_) => 1,
        CanonicalQueryNode::Tuple(_) => 2,
        CanonicalQueryNode::Collection(_) => 3,
        CanonicalQueryNode::Map(_) => 4,
    };
    let mut pending = vec![Task::Nodes(left, right)];
    while let Some(task) = pending.pop() {
        let (left, right) = match task {
            Task::Length(left, right) => {
                let order = left.cmp(&right);
                if order != Ordering::Equal {
                    return order;
                }
                continue;
            }
            Task::Nodes(left, right) if left == right => continue,
            Task::Nodes(left, right) => (&arena[left], &arena[right]),
        };
        let order = rank(left).cmp(&rank(right));
        if order != Ordering::Equal {
            return order;
        }
        match (left, right) {
            (CanonicalQueryNode::Nil, CanonicalQueryNode::Nil) => {}
            (CanonicalQueryNode::Scalar(left), CanonicalQueryNode::Scalar(right)) => {
                let order = left.index_cmp(right);
                if order != Ordering::Equal {
                    return order;
                }
            }
            (CanonicalQueryNode::Tuple(left), CanonicalQueryNode::Tuple(right))
            | (CanonicalQueryNode::Collection(left), CanonicalQueryNode::Collection(right)) => {
                pending.push(Task::Length(left.len(), right.len()));
                for (left, right) in left.iter().zip(right).rev() {
                    pending.push(Task::Nodes(*left, *right));
                }
            }
            (CanonicalQueryNode::Map(left), CanonicalQueryNode::Map(right)) => {
                pending.push(Task::Length(left.len(), right.len()));
                for ((left_key, left_value), (right_key, right_value)) in
                    left.iter().zip(right).rev()
                {
                    pending.push(Task::Nodes(*left_value, *right_value));
                    pending.push(Task::Nodes(*left_key, *right_key));
                }
            }
            _ => unreachable!("equal canonical query-value ranks have matching variants"),
        }
    }
    Ordering::Equal
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
    /// Cooperative evaluator and projection work charged to QueryControl.
    pub work: u64,
    pub clauses_executed: u64,
    pub datoms_examined: u64,
    pub index_seeks: u64,
    pub rows_produced: u64,
    pub rule_iterations: u64,
    pub prepared_cache_hits: u64,
    pub hash_join_build_rows: u64,
    pub hash_join_probes: u64,
    pub join_candidates: u64,
    pub grouped_probes_saved: u64,
    /// Conservative auxiliary join-table retention estimate, not measured
    /// allocator usage or process RSS.
    pub peak_join_bytes: usize,
    /// Accounted row/value allocation bytes over execution, not process RSS.
    pub allocated_value_bytes: usize,
    pub fulltext_searches: u64,
    /// Search indexes can lag even when the supplied database value is fixed.
    pub fulltext_lagging_searches: u64,
    pub fulltext_truncated_searches: u64,
    pub fulltext_read_bytes: u64,
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
    /// Reference mode used by differential tests: disable hash joins/grouped
    /// probes and evaluate native database patterns through EAVT scans.
    pub force_scan: bool,
    /// Auxiliary hash tables and native probe groups are chunked using this
    /// retention allowance. One ordinary native probe is always admitted even
    /// when its key exceeds the allowance; valid data is not rejected. Zero
    /// disables hash joins and groups native probes one row at a time.
    pub max_join_bytes: usize,
    /// Maximum estimated temporary/output allocation of an exact numeric
    /// operation. Compact decimal exponents do not themselves consume bytes.
    /// Program queries additionally obey their shared remaining value budget.
    pub max_numeric_bytes: usize,
}

impl Default for QueryControl {
    fn default() -> Self {
        Self {
            timeout: None,
            max_work: usize::MAX,
            max_intermediate_rows: usize::MAX,
            max_result_rows: usize::MAX,
            cancel: Arc::new(AtomicBool::new(false)),
            force_scan: false,
            max_join_bytes: 4 * 1024 * 1024,
            max_numeric_bytes: 16 * 1024 * 1024,
        }
    }
}

pub struct QueryEngine;

type NativeQueryFunction = dyn Fn(&DatabaseValue, &[Value], &QueryControl) -> Result<Vec<Vec<Value>>, SemanticError>
    + Send
    + Sync;

#[derive(Clone)]
struct QueryExtension {
    implementation: QueryExtensionImplementation,
    exact_hash: Option<ProgramHash>,
}

#[derive(Clone)]
enum QueryExtensionImplementation {
    Local(Arc<NativeQueryFunction>),
    Program(Arc<Program>),
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

    /// Register trusted peer-local Rust code. The callback receives remaining
    /// work/time allowances; cancellation and deadline checks surround it.
    /// Arbitrary Rust code is cooperative, not forcibly preempted or metered.
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
                implementation: QueryExtensionImplementation::Local(Arc::new(function)),
                exact_hash: None,
            },
        );
    }

    /// Register controlled native bytecode. Interpreter fuel is charged to
    /// the enclosing query, and siblings share its original absolute deadline.
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
        program.validate()?;
        self.functions.insert(
            name.into(),
            QueryExtension {
                implementation: QueryExtensionImplementation::Program(Arc::new(program)),
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
        deadline: Option<Instant>,
    ) -> (Result<Vec<Vec<Value>>, SemanticError>, usize) {
        let mut work = 0;
        let result = catch_unwind(AssertUnwindSafe(|| {
            let extension = self.functions.get(name).ok_or_else(|| {
                SemanticError::incorrect(
                    "query/unknown-extension",
                    format!("unknown query extension {name}"),
                )
            })?;
            match &extension.implementation {
                QueryExtensionImplementation::Local(callback) => {
                    callback(database, arguments, control)
                }
                QueryExtensionImplementation::Program(program) => {
                    if control.max_work == 0 {
                        return Err(resource(
                            "query/work-limit",
                            "query has no remaining program fuel",
                        ));
                    }
                    let fuel = u64::try_from(control.max_work).unwrap_or(u64::MAX);
                    let program_control = ProgramControl {
                        fuel,
                        max_stack: control.max_intermediate_rows,
                        max_value_bytes: control.max_work,
                        max_collection_items: control.max_intermediate_rows,
                        max_forms: control.max_result_rows,
                        max_output: control.max_work,
                        max_calls: 1,
                        cancelled: Some(control.cancel.as_ref()),
                    };
                    let mut budget = crate::ProgramBudget::new(program_control)?
                        .with_deadline(deadline)
                        .with_query_numeric_bytes(control.max_numeric_bytes);
                    let result = ProgramRuntime.execute_query_with_budget(
                        program,
                        database,
                        arguments,
                        &mut budget,
                    );
                    work = usize::try_from(fuel - budget.remaining_fuel()).unwrap_or(usize::MAX);
                    match result {
                        Ok(ProgramOutput::Query(rows)) => Ok(rows),
                        Ok(_) => unreachable!("program kind was checked"),
                        Err(error) if error.code == "program/fuel-exhausted" => Err(resource(
                            "query/work-limit",
                            "query program exhausted the remaining shared work",
                        )),
                        Err(error) => Err(error),
                    }
                }
            }
        }))
        .map_err(|_| {
            SemanticError::new(
                ErrorCategory::Fault,
                "query/local-extension-panicked",
                format!("query extension {name} panicked"),
            )
        })
        .and_then(|result| result);
        (result, work)
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
    /// Query-only containers never enter persisted values or index keys.
    /// Construction normalizes nil/scalar leaves to the variants above.
    Query(QueryValue),
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
            Self::Nil | Self::Query(_) => None,
            Self::Stored(value) => Some(value),
        }
    }

    fn index_cmp(&self, other: &Self) -> std::cmp::Ordering {
        match (self, other) {
            (Self::Nil, Self::Nil) => std::cmp::Ordering::Equal,
            (Self::Nil, _) | (Self::Stored(_), Self::Query(_)) => std::cmp::Ordering::Less,
            (_, Self::Nil) | (Self::Query(_), Self::Stored(_)) => std::cmp::Ordering::Greater,
            (Self::Stored(left), Self::Stored(right)) => left.index_cmp(right),
            (Self::Query(left), Self::Query(right)) => left.canonical_cmp(right),
        }
    }

    fn query_value(&self) -> QueryValue {
        match self {
            Self::Nil => QueryValue::Nil,
            Self::Stored(value) => QueryValue::Scalar(value.clone()),
            Self::Query(value) => value.clone(),
        }
    }

    fn from_query_value(value: QueryValue) -> Self {
        match &value {
            QueryValue::Nil => Self::Nil,
            QueryValue::Scalar(_) => Self::Stored(value.into_scalar().expect("scalar variant")),
            QueryValue::Tuple(_) => {
                query_tuple_value(&value).map_or_else(|| Self::Query(value), Self::Stored)
            }
            _ => Self::Query(value),
        }
    }

    /// One-level sequence destructuring; nested containers remain intact.
    fn sequence_values(&self) -> Option<Vec<Self>> {
        match self {
            Self::Stored(Value::Tuple(values)) => Some(
                values
                    .iter()
                    .cloned()
                    .map(|value| value.map_or(Self::Nil, Self::Stored))
                    .collect(),
            ),
            Self::Query(QueryValue::Tuple(values) | QueryValue::Collection(values)) => {
                Some(values.iter().cloned().map(Self::from_query_value).collect())
            }
            _ => None,
        }
    }
}

/// A sequential tuple of stored values is the same legal tuple join key
/// whether constructed by `tuple` or returned by a tuple-shaped subquery.
/// Traverse iteratively; maps/collections remain query-only containers.
fn query_tuple_value(value: &QueryValue) -> Option<Value> {
    enum Task<'a> {
        Value(&'a QueryValue),
        Tuple(usize),
    }
    let mut pending = vec![Task::Value(value)];
    let mut values = Vec::new();
    while let Some(task) = pending.pop() {
        match task {
            Task::Value(QueryValue::Nil) => values.push(None),
            Task::Value(QueryValue::Scalar(value)) => values.push(Some(value.clone())),
            Task::Value(QueryValue::Tuple(children)) => {
                pending.push(Task::Tuple(children.len()));
                pending.extend(children.iter().rev().map(Task::Value));
            }
            Task::Value(QueryValue::Collection(_) | QueryValue::Map(_)) => return None,
            Task::Tuple(count) => {
                let children = values.split_off(values.len() - count);
                values.push(Some(Value::Tuple(children)));
            }
        }
    }
    values.pop().flatten()
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
    sources: BTreeMap<&'a str, SourceRef<'a>>,
    control: &'a QueryControl,
    deadline: Option<Instant>,
    work: usize,
    stats: QueryStats,
    plan: Vec<PlanStep>,
    extensions: Option<&'a QueryExtensions>,
    rule_memo: BTreeMap<RuleInvocationKey, Vec<Vec<BoundValue>>>,
    negative_memo: BTreeMap<dependencies::NegativeInvocation, bool>,
    solving_rules: bool,
    rule_memo_complete: bool,
    defer_rules: bool,
    borrowed_cancel: Option<&'a AtomicBool>,
    max_value_bytes: usize,
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
        let sources: Vec<_> = sources
            .iter()
            .map(|source| QueryDataSource::database(&source.name, source.database.clone()))
            .collect();
        Self::execute_sources_with_extensions(query, &sources, inputs, control, extensions)
    }

    pub fn execute_sources(
        query: &Query,
        sources: &[QueryDataSource],
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        Self::execute_sources_with_extensions(query, sources, inputs, control, None)
    }

    pub fn execute_sources_with_extensions(
        query: &Query,
        sources: &[QueryDataSource],
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: Option<&QueryExtensions>,
    ) -> Result<QueryOutcome, SemanticError> {
        let (prepared, hit) = prepare::cached(query)?;
        let query = prepared.as_ref().map_or(query, PreparedQuery::query);
        let mut outcome = Self::execute_query(query, sources, inputs, control, extensions)?;
        outcome.stats.prepared_cache_hits = u64::from(hit);
        Ok(outcome)
    }

    fn execute_prepared(
        prepared: &PreparedQuery,
        sources: &[QueryDataSource],
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: Option<&QueryExtensions>,
    ) -> Result<QueryOutcome, SemanticError> {
        Self::execute_query(prepared.query(), sources, inputs, control, extensions)
    }

    fn execute_query(
        query: &Query,
        sources: &[QueryDataSource],
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: Option<&QueryExtensions>,
    ) -> Result<QueryOutcome, SemanticError> {
        let mut source_map = BTreeMap::new();
        for source in sources {
            if source_map
                .insert(source.name.as_str(), source.borrowed())
                .is_some()
            {
                return Err(SemanticError::incorrect(
                    "query/duplicate-source",
                    format!("source {} is supplied more than once", source.name),
                ));
            }
        }
        let deadline = control
            .timeout
            .and_then(|timeout| Instant::now().checked_add(timeout));
        let mut state = State {
            sources: source_map,
            control,
            deadline,
            work: 0,
            stats: QueryStats::default(),
            plan: Vec::new(),
            extensions,
            rule_memo: BTreeMap::new(),
            negative_memo: BTreeMap::new(),
            solving_rules: false,
            rule_memo_complete: false,
            defer_rules: false,
            borrowed_cancel: None,
            max_value_bytes: usize::MAX,
        };
        let result = run_query(query, inputs, &mut state)?;
        Ok(QueryOutcome {
            result,
            stats: state.stats,
            plan: state.plan,
        })
    }

    pub(crate) fn execute_program(
        query: &Query,
        sources: &[QueryDataSource],
        inputs: &[QueryInput],
        budget: &mut crate::ProgramBudget<'_>,
    ) -> Result<QueryOutcome, SemanticError> {
        let control = QueryControl {
            max_work: usize::try_from(budget.remaining_fuel()).unwrap_or(usize::MAX),
            max_intermediate_rows: budget.query_row_limit(),
            max_result_rows: budget.query_row_limit(),
            max_join_bytes: budget.query_remaining_value_bytes().min(4 * 1024 * 1024),
            max_numeric_bytes: budget.query_numeric_bytes(),
            ..QueryControl::default()
        };
        let mut source_map = BTreeMap::new();
        for source in sources {
            if source_map
                .insert(source.name.as_str(), source.borrowed())
                .is_some()
            {
                return Err(SemanticError::incorrect(
                    "query/duplicate-source",
                    "duplicate program query source",
                ));
            }
        }
        let (result, work, bytes) = {
            let mut state = State {
                sources: source_map,
                control: &control,
                deadline: budget.query_deadline(),
                work: 0,
                stats: QueryStats::default(),
                plan: Vec::new(),
                extensions: None,
                rule_memo: BTreeMap::new(),
                negative_memo: BTreeMap::new(),
                solving_rules: false,
                rule_memo_complete: false,
                defer_rules: false,
                borrowed_cancel: budget.query_cancelled(),
                max_value_bytes: budget.query_remaining_value_bytes(),
            };
            let result = (|| {
                state.check(1)?;
                let (prepared, hit) = prepare::cached(query)?;
                state.stats.prepared_cache_hits = u64::from(hit);
                let result = run_query(
                    prepared.as_ref().map_or(query, PreparedQuery::query),
                    inputs,
                    &mut state,
                )?;
                Ok(QueryOutcome {
                    result,
                    stats: state.stats.clone(),
                    plan: std::mem::take(&mut state.plan),
                })
            })();
            (result, state.work as u64, state.stats.allocated_value_bytes)
        };
        // Debit attempted work even if cancellation, a source error, or a query
        // limit interrupted execution. Never grant a fresh interpreter budget.
        let work_charge = budget.charge_query_work(work);
        let bytes_charge = budget.charge_query_bytes(bytes);
        match result {
            Ok(outcome) => {
                work_charge?;
                bytes_charge?;
                Ok(outcome)
            }
            Err(error) => Err(error),
        }
    }
}

pub(crate) fn validate_program_query(
    query: &Query,
    input_count: usize,
) -> Result<(), SemanticError> {
    validate_query(query, input_count)
}

fn run_query(
    query: &Query,
    inputs: &[QueryInput],
    state: &mut State<'_>,
) -> Result<QueryResult, SemanticError> {
    state.check(0)?;
    if query.inputs.len() != inputs.len() {
        return Err(SemanticError::incorrect(
            "query/input-arity",
            "query input count does not match its bindings",
        ));
    }
    // These checks depend on the invocation's sources, never cached preparation.
    validate_consumed_sources(query, &state.sources)?;
    dependencies::validate_negation(query, state)?;
    let initial = bind_inputs(&query.inputs, inputs, state)?;
    let rows = dependencies::evaluate_complete(query, initial, state)?;
    let mut pull_budget = QueryPullBudget::new(
        Arc::clone(&state.control.cancel),
        state.deadline,
        state.control.max_work,
        state.work,
    )
    .with_borrowed_cancel(state.borrowed_cancel)
    .with_value_budget(state.stats.allocated_value_bytes, state.max_value_bytes);
    let result = shape_results(
        query,
        rows,
        state.control.max_result_rows,
        &state.sources,
        &mut pull_budget,
        state.control.max_numeric_bytes,
    );
    state.work = pull_budget.work();
    state.stats.allocated_value_bytes = pull_budget.value_bytes();
    state.stats.work = state.work as u64;
    let result = result?;
    state.stats.rows_produced = result_len(&result) as u64;
    Ok(result)
}

fn validate_consumed_sources(
    query: &Query,
    sources: &BTreeMap<&str, SourceRef<'_>>,
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
            find_source(sources, source)?.require_point_in_time("pull")?;
        }
    }
    validate_nested_sources(
        &query.clauses,
        &query.rules,
        None,
        sources,
        &mut BTreeSet::new(),
    )?;
    Ok(())
}

// Validate semantic source consumers even when an outer relation is empty.
// Subquery rule scopes are independent; inherited `$` must follow the same
// mapping as runtime invocation, including queries nested in rule bodies.
fn validate_nested_sources(
    clauses: &[Clause],
    rules: &[Rule],
    inherited: Option<&str>,
    sources: &BTreeMap<&str, SourceRef<'_>>,
    visited: &mut BTreeSet<(String, String)>,
) -> Result<(), SemanticError> {
    let mut pending: Vec<_> = clauses
        .iter()
        .rev()
        .map(|clause| (clause, inherited.map(str::to_owned)))
        .collect();
    while let Some((clause, inherited_name)) = pending.pop() {
        let inherited = inherited_name.as_deref();
        match clause {
            Clause::Pattern(pattern) => {
                let name = effective_source(&pattern.source, inherited);
                match sources.get(name) {
                    Some(SourceRef::Database(database)) => {
                        if let Term::Constant(attribute) = &pattern.attribute {
                            attribute_value(database, attribute)?;
                        }
                    }
                    Some(SourceRef::Tuples(_)) => {}
                    Some(SourceRef::Log(_)) => {
                        return Err(SemanticError::incorrect(
                            "query/source-kind",
                            "log sources require tx-ids/tx-data",
                        ));
                    }
                    None => {
                        return Err(SemanticError::incorrect(
                            "query/unknown-source",
                            format!("unknown source {name}"),
                        ));
                    }
                }
            }
            Clause::Function {
                function: Function::TxIds | Function::TxData,
                source,
                ..
            } => {
                let name = effective_source(source, inherited);
                if !matches!(sources.get(name), Some(SourceRef::Log(_))) {
                    return Err(SemanticError::incorrect(
                        "query/source-kind",
                        "transaction functions require an immutable log source",
                    ));
                }
            }
            Clause::Function {
                function: Function::Fulltext,
                source,
                args,
                ..
            } => {
                let database = find_source(sources, effective_source(source, inherited))?;
                if args.len() != 2 {
                    return Err(SemanticError::incorrect(
                        "query/function-arity",
                        "fulltext requires attribute and search string",
                    ));
                }
                // Per-run validation is retained even when an earlier clause
                // produces no rows or the structural prepared query is reused.
                if let Term::Constant(attribute) = &args[0] {
                    let attribute = attribute_value(database, attribute)?;
                    if !database.schema().attribute(attribute)?.fulltext {
                        return Err(SemanticError::incorrect(
                            "query/not-fulltext-attribute",
                            "fulltext requires an attribute installed with fulltext enabled",
                        ));
                    }
                }
                if let Term::Constant(search) = &args[1] {
                    if !matches!(search, Value::String(_)) {
                        return Err(SemanticError::incorrect(
                            "query/fulltext-search",
                            "fulltext search must be a string",
                        ));
                    }
                }
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
                find_source(sources, effective_source(source, inherited))?;
            }
            Clause::Function {
                function: Function::Query(query),
                source,
                ..
            } => {
                let selected = effective_source(source, inherited);
                let database = *sources.get(selected).ok_or_else(|| {
                    SemanticError::incorrect(
                        "query/unknown-source",
                        format!("unknown source {selected}"),
                    )
                })?;
                let mut nested = sources.clone();
                nested.insert("$", database);
                validate_consumed_sources(query, &nested)?;
            }
            Clause::Not { clauses, .. } => {
                pending.extend(
                    clauses
                        .iter()
                        .rev()
                        .map(|clause| (clause, inherited_name.clone())),
                );
            }
            Clause::Or { branches, .. } => {
                pending.extend(
                    branches
                        .iter()
                        .rev()
                        .flat_map(|branch| branch.iter().rev())
                        .map(|clause| (clause, inherited_name.clone())),
                );
            }
            Clause::Rule { source, name, .. } => {
                let source = effective_source(source, inherited);
                if visited.insert((name.clone(), source.into())) {
                    for rule in rules.iter().rev().filter(|rule| rule.name == *name) {
                        pending.extend(
                            rule.clauses
                                .iter()
                                .rev()
                                .map(|clause| (clause, Some(source.to_owned()))),
                        );
                    }
                }
            }
            _ => {}
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
    let mut pending: Vec<_> = clauses
        .iter()
        .rev()
        .map(|clause| (clause, inherited_source.map(str::to_owned)))
        .collect();
    while let Some((clause, inherited_name)) = pending.pop() {
        let inherited_source = inherited_name.as_deref();
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
                function:
                    Function::GetElse
                    | Function::GetSome
                    | Function::Extension(_)
                    | Function::Query(_)
                    | Function::TxIds
                    | Function::TxData,
                source,
                ..
            }
            | Clause::Function {
                function: Function::Fulltext,
                source,
                ..
            } => {
                consumed.insert(effective_source(source, inherited_source).to_owned());
            }
            Clause::Not { clauses, .. } => {
                pending.extend(
                    clauses
                        .iter()
                        .rev()
                        .map(|clause| (clause, inherited_name.clone())),
                );
            }
            Clause::Or { branches, .. } => {
                pending.extend(
                    branches
                        .iter()
                        .rev()
                        .flat_map(|branch| branch.iter().rev())
                        .map(|clause| (clause, inherited_name.clone())),
                );
            }
            Clause::Rule { source, name, .. } => {
                let source = effective_source(source, inherited_source).to_owned();
                if visited_rules.insert((name.clone(), source.clone())) {
                    for rule in rules.iter().rev().filter(|rule| rule.name == *name) {
                        pending.extend(
                            rule.clauses
                                .iter()
                                .rev()
                                .map(|clause| (clause, Some(source.clone()))),
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

fn validate_query(query: &Query, input_count: usize) -> Result<(), SemanticError> {
    if query.clauses.is_empty() && query.inputs.is_empty() {
        return Err(SemanticError::incorrect(
            "query/no-input-or-where",
            "query requires input bindings or where clauses",
        ));
    }
    if query.inputs.len() != input_count {
        return Err(SemanticError::incorrect(
            "query/input-arity",
            format!(
                "expected {} inputs, got {}",
                query.inputs.len(),
                input_count
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
                function: Function::Fulltext,
                args,
                ..
            } if args.len() != 2 => {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "fulltext requires attribute and search string",
                ));
            }
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

fn bind_inputs(
    specs: &[InputSpec],
    values: &[QueryInput],
    state: &mut State<'_>,
) -> Result<Vec<Row>, SemanticError> {
    state.check(0)?;
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
        let next = join::input_join(&rows, &relation, state)?;
        rows = dedupe_rows(next);
        state.check(0)?;
        if rows.len() > state.control.max_intermediate_rows {
            return Err(resource(
                "query/intermediate-limit",
                "query input relation exceeded its row limit",
            ));
        }
    }
    Ok(rows)
}

fn evaluate_clauses(
    clauses: &[Clause],
    mut rows: Vec<Row>,
    rules: &[Rule],
    inherited_source: Option<&str>,
    state: &mut State<'_>,
) -> EvaluationResult<Vec<Row>> {
    let mut remaining: Vec<_> = clauses.iter().collect();
    while !remaining.is_empty() {
        state.check(1)?;
        let sample = rows.first().cloned().unwrap_or_default();
        let mut selected = None;
        for (index, clause) in remaining.iter().enumerate() {
            if clause_ready(clause, &sample, rules, state)? {
                let score = clause_score(clause, &sample);
                if selected.is_none_or(|(_, best)| score >= best) {
                    selected = Some((index, score));
                }
            }
        }
        let selected = selected.map(|(index, _)| index).ok_or_else(|| {
            SemanticError::incorrect(
                "query/insufficient-binding",
                "no remaining clause has its required variables bound",
            )
        })?;
        let clause = remaining.remove(selected);
        let before = rows.len();
        let (next, access) =
            evaluate_clause(clause, rows, rules, inherited_source, clauses, state)?;
        rows = dedupe_rows(next);
        if rows.len() > state.control.max_intermediate_rows {
            return Err(resource(
                "query/intermediate-limit",
                "query intermediate relation exceeded its row limit",
            )
            .into());
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
    conjunction: &[Clause],
    state: &mut State<'_>,
) -> EvaluationResult<(Vec<Row>, String)> {
    match clause {
        Clause::Pattern(pattern) => Ok(evaluate_pattern(
            pattern,
            rows,
            inherited_source,
            conjunction,
            state,
        )?),
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
                    state.push_row(&mut next, row)?;
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
                        state.push_row(&mut next, bound)?;
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
            let mut pending = BTreeSet::new();
            for row in rows {
                if required.iter().any(|variable| !row.contains_key(variable)) {
                    return Err(SemanticError::incorrect(
                        "query/insufficient-binding",
                        "not clause has unbound join variables",
                    )
                    .into());
                }
                let seed = if join.is_some() {
                    project_row(&row, &required)
                } else {
                    row.clone()
                };
                match dependencies::evaluate_negative(clause, seed, inherited_source, state)? {
                    dependencies::NegativeStatus::Complete(true) => {
                        state.push_row(&mut next, row)?
                    }
                    dependencies::NegativeStatus::Complete(false) => {}
                    dependencies::NegativeStatus::Pending(key) => {
                        pending.insert(key);
                    }
                }
            }
            if !pending.is_empty() {
                return Err(dependencies::EvaluationError::AwaitNegative(
                    pending.into_iter().collect(),
                ));
            }
            Ok((next, "set-difference".into()))
        }
        Clause::Or { join, branches } => {
            validate_or(branches, join.as_deref())?;
            let mut next = Vec::new();
            let mut pending = BTreeSet::new();
            let mut deferred = false;
            for row in rows {
                for branch in branches {
                    let seed = join
                        .as_ref()
                        .map_or_else(|| row.clone(), |variables| project_row(&row, variables));
                    let produced = match evaluate_clauses(
                        branch,
                        vec![seed],
                        rules,
                        inherited_source,
                        state,
                    ) {
                        Ok(rows) => rows,
                        Err(dependencies::EvaluationError::AwaitNegative(requests)) => {
                            pending.extend(requests);
                            continue;
                        }
                        Err(dependencies::EvaluationError::AwaitRules) => {
                            deferred = true;
                            continue;
                        }
                        Err(error) => return Err(error),
                    };
                    for produced in produced {
                        if let Some(join) = join {
                            let mut merged = row.clone();
                            if join.iter().all(|variable| {
                                produced.get(variable).is_none_or(|value| {
                                    unify_variable(&mut merged, variable, value)
                                })
                            }) {
                                state.push_row(&mut next, merged)?;
                            }
                        } else {
                            state.push_row(&mut next, produced)?;
                        }
                    }
                }
            }
            if !pending.is_empty() {
                return Err(dependencies::EvaluationError::AwaitNegative(
                    pending.into_iter().collect(),
                ));
            }
            if deferred {
                return Err(dependencies::EvaluationError::AwaitRules);
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
                )
                .into());
            }
            let mut invocations = Vec::new();
            for row in rows {
                state.check(1)?;
                if required
                    .iter()
                    .any(|index| !term_is_bound(&args[*index], &row))
                {
                    return Err(SemanticError::incorrect(
                        "query/insufficient-binding",
                        format!("rule {name} requires its declared input positions to be bound"),
                    )
                    .into());
                }
                let key = RuleInvocationKey {
                    source: source.to_owned(),
                    name: name.clone(),
                    bindings: args
                        .iter()
                        .map(|term| term_bound_value(term, &row))
                        .collect(),
                };
                ensure_rule_memo_entry(state, key.clone())?;
                invocations.push((row, key));
            }
            // Demand is a relation, not one invocation at a time. In particular
            // collect every row's negative dependencies before restarting a scan.
            solve_rule_invocations(rules, state)?;
            let mut next = Vec::new();
            for (row, key) in invocations {
                let relation = rule_memo_rows(state, &key)?;
                for tuple in &relation {
                    if tuple.len() != args.len() {
                        return Err(fault(
                            "query/rule-relation-width",
                            "compiled rule relation has the wrong width",
                        )
                        .into());
                    }
                    let mut candidate = row.clone();
                    if args
                        .iter()
                        .zip(tuple)
                        .all(|(term, value)| unify_term(&mut candidate, term, value))
                    {
                        state.push_row(&mut next, candidate)?;
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
    conjunction: &[Clause],
    state: &mut State<'_>,
) -> Result<(Vec<Row>, String), SemanticError> {
    let source = effective_source(&pattern.source, inherited_source);
    let source_value = state.sources.get(source).copied().ok_or_else(|| {
        SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
    })?;
    let database = match source_value {
        SourceRef::Database(database) => database,
        SourceRef::Tuples(tuples) => return join::evaluate_tuples(pattern, rows, tuples, state),
        SourceRef::Log(_) => {
            return Err(SemanticError::incorrect(
                "query/source-kind",
                "log sources must be consumed with tx-ids/tx-data",
            ));
        }
    };
    let mut next = Vec::new();
    let mut access = "EAVT scan".to_owned();
    let mut input = rows.into_iter().peekable();
    while input.peek().is_some() {
        // Group identical *resolved* probes only within a bounded batch. The
        // selected native index remains the same as in the one-row evaluator.
        let mut groups = BTreeMap::<_, Vec<Row>>::new();
        let mut bytes = 0usize;
        while let Some(row) = input.next() {
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
            let range = if !state.control.force_scan
                && entity.is_none()
                && bound_value.is_none()
                && attribute.is_some_and(|attribute| database.physical_avet_ready(attribute))
            {
                ranges::constraints(pattern, conjunction, &row, state)?
            } else {
                None
            };
            let key = (entity, attribute, value.map(BoundValue::Stored), range);
            bytes = bytes.saturating_add(
                std::mem::size_of::<Row>()
                    + std::mem::size_of_val(&key)
                    + key.2.as_ref().map_or(0, join::bound_bytes)
                    + key.3.as_ref().map_or(0, ranges::Range::retained_bytes),
            );
            groups.entry(key).or_default().push(row);
            if state.control.force_scan || bytes >= state.control.max_join_bytes {
                break;
            }
        }
        state.stats.peak_join_bytes = state.stats.peak_join_bytes.max(bytes);
        for ((entity, attribute, value, range), rows) in groups {
            let value = value.as_ref().and_then(BoundValue::stored);
            state.stats.grouped_probes_saved += rows.len().saturating_sub(1) as u64;
            let (mut datoms, selected) = select_datoms(
                database,
                entity,
                attribute,
                value,
                range,
                state.control.force_scan,
            )?;
            access = selected;
            state.stats.index_seeks += datoms.initial_seeks();
            while let Some(datom) = datoms.next(state) {
                let datom = datom?;
                for row in &rows {
                    state.check(1)?;
                    state.stats.join_candidates += 1;
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
                        value,
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
                        state.push_row(&mut next, candidate)?;
                    }
                }
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
    range: Option<ranges::Range>,
    force_scan: bool,
) -> Result<(ranges::Datoms<'a>, String), SemanticError> {
    if !force_scan {
        if let Some(entity) = entity {
            let prefix = IndexPrefix::Eavt {
                entity,
                attribute,
                value: attribute.and(value).cloned(),
            };
            return Ok((
                ranges::Datoms::prefix(database.query_prefix_cursor(&prefix)?),
                "EAVT seek".into(),
            ));
        }
        if let Some(attribute) = attribute {
            if let Some(value) = value
                && database.physical_avet_ready(attribute)
            {
                let prefix = IndexPrefix::Avet {
                    attribute,
                    value: Some(value.clone()),
                    entity: None,
                };
                return Ok((
                    ranges::Datoms::prefix(database.query_prefix_cursor(&prefix)?),
                    "AVET seek".into(),
                ));
            }
            if let Some(range) = range {
                return Ok((
                    ranges::Datoms::range(database, attribute, range)?,
                    "AVET range".into(),
                ));
            }
            let prefix = IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            };
            return Ok((
                ranges::Datoms::prefix(database.query_prefix_cursor(&prefix)?),
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
                ranges::Datoms::prefix(database.query_prefix_cursor(&prefix)?),
                "VAET seek".into(),
            ));
        }
    }
    Ok((
        ranges::Datoms::scan(database.query_scan_cursor(IndexOrder::Eavt)?),
        "EAVT scan".into(),
    ))
}

fn clause_ready(
    clause: &Clause,
    row: &Row,
    rules: &[Rule],
    state: &mut State<'_>,
) -> Result<bool, SemanticError> {
    Ok(match clause {
        Clause::Pattern(_) => true,
        Clause::Or { .. } => dependencies::ready(clause, row, rules, state)?,
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
    })
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

fn solve_rule_invocations(rules: &[Rule], state: &mut State<'_>) -> EvaluationResult<()> {
    // Recovered `eval-query` keys its input work by source and adorned
    // predicate, accumulates answers in `ans`, and the outer `qsqr` loop
    // repeats until answer cardinalities stop changing. This memo uses the
    // concrete bound values as well as the adornment: it is more selective,
    // while retaining the same monotone fixed-point boundary.
    if !state.solving_rules && !state.rule_memo_complete {
        if state.defer_rules {
            // A sibling already requested negative work during this pass.
            // Register the new keys but let the driver solve them as one batch.
            return Err(dependencies::EvaluationError::AwaitRules);
        }
        state.solving_rules = true;
        let result = stabilize_rule_memo(rules, state);
        state.solving_rules = false;
        if matches!(
            &result,
            Err(dependencies::EvaluationError::AwaitNegative(_))
        ) {
            state.defer_rules = true;
        }
        result?;
        state.rule_memo_complete = true;
    }
    Ok(())
}

fn stabilize_rule_memo(rules: &[Rule], state: &mut State<'_>) -> EvaluationResult<()> {
    loop {
        state.check(1)?;
        let before_entries = state.rule_memo.len();
        let before_rows = state.rule_memo.values().map(Vec::len).sum::<usize>();
        let keys = state.rule_memo.keys().cloned().collect::<Vec<_>>();
        let mut pending = BTreeSet::new();

        for key in keys {
            let produced = match evaluate_rule_key(&key, rules, state) {
                Ok(rows) => rows,
                Err(dependencies::EvaluationError::AwaitNegative(requests)) => {
                    pending.extend(requests);
                    continue;
                }
                Err(error) => return Err(error),
            };
            ensure_rule_memo_entry(state, key.clone())?;
            let (length, added) = {
                let rows = state
                    .rule_memo
                    .get_mut(&key)
                    .expect("rule memo entry was ensured");
                let prior = rows.len();
                rows.extend(produced);
                stable_dedupe_by(rows, Ord::cmp);
                (rows.len(), rows.len() - prior)
            };
            state.check(added)?;
            if length > state.control.max_intermediate_rows {
                return Err(resource(
                    "query/intermediate-limit",
                    "rule memo relation exceeded the intermediate row limit",
                )
                .into());
            }
        }

        if !pending.is_empty() {
            return Err(dependencies::EvaluationError::AwaitNegative(
                pending.into_iter().collect(),
            ));
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
) -> EvaluationResult<Vec<Vec<BoundValue>>> {
    let mut produced = Vec::new();
    let mut pending = BTreeSet::new();
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
        let rows =
            match evaluate_clauses(&rule.clauses, vec![seed], rules, Some(&key.source), state) {
                Ok(rows) => rows,
                Err(dependencies::EvaluationError::AwaitNegative(requests)) => {
                    pending.extend(requests);
                    continue;
                }
                Err(error) => return Err(error),
            };
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
            state.charge_value_bytes(tuple.iter().fold(0usize, |bytes, value| {
                bytes.saturating_add(join::bound_bytes(value))
            }))?;
            produced.push(tuple);
        }
    }
    if !pending.is_empty() {
        return Err(dependencies::EvaluationError::AwaitNegative(
            pending.into_iter().collect(),
        ));
    }
    stable_dedupe_by(&mut produced, Ord::cmp);
    Ok(produced)
}

fn ensure_rule_memo_entry(
    state: &mut State<'_>,
    key: RuleInvocationKey,
) -> Result<(), SemanticError> {
    if !state.rule_memo.contains_key(&key) {
        state.rule_memo_complete = false;
        state.charge_value_bytes(key.bindings.iter().flatten().fold(
            std::mem::size_of::<RuleInvocationKey>() + key.name.len() + key.source.len(),
            |bytes, value| bytes.saturating_add(join::bound_bytes(value)),
        ))?;
    }
    state.rule_memo.entry(key).or_default();
    Ok(())
}

fn rule_memo_rows(
    state: &mut State<'_>,
    key: &RuleInvocationKey,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    let bytes = state
        .rule_memo
        .get(key)
        .into_iter()
        .flatten()
        .flatten()
        .fold(0usize, |bytes, value| {
            bytes.saturating_add(join::bound_bytes(value))
        });
    state.charge_value_bytes(bytes)?;
    Ok(state.rule_memo.get(key).cloned().unwrap_or_default())
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
            let database = source_database(state, source)?;
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
    state: &mut State<'_>,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    match function {
        Function::TxIds | Function::TxData => {
            sources::log_function(function, source, args, binding, state)
        }
        Function::Fulltext => fulltext::execute(source, args, binding, state),
        Function::Query(query) => nested::execute(&query, source, args, binding, state),
        Function::Ground => ground_output(args, binding),
        Function::Tuple => Ok(vec![vec![if args
            .iter()
            .any(|value| matches!(value, BoundValue::Query(_)))
        {
            BoundValue::Query(QueryValue::Tuple(
                args.iter().map(BoundValue::query_value).collect(),
            ))
        } else {
            BoundValue::Stored(Value::Tuple(
                args.iter().map(|value| value.stored().cloned()).collect(),
            ))
        }]]),
        Function::Untuple => match args {
            [BoundValue::Stored(Value::Tuple(values))] => Ok(vec![
                values
                    .iter()
                    .cloned()
                    .map(|value| value.map_or(BoundValue::Nil, BoundValue::Stored))
                    .collect(),
            ]),
            [value @ BoundValue::Query(QueryValue::Tuple(_))] => {
                Ok(vec![value.sequence_values().expect("tuple variant")])
            }
            _ => Err(SemanticError::incorrect(
                "query/function-type",
                "untuple requires one tuple",
            )),
        },
        Function::Add | Function::Subtract | Function::Multiply | Function::Divide => {
            numeric_function(function, args, state)
                .map(|value| vec![vec![BoundValue::Stored(value)]])
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
            state.check(1)?;
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
            // Native callbacks are trusted cooperative code, but receive only
            // the parent's remaining allowance. Persisted programs report
            // interpreter fuel back into that same allowance on every call.
            let mut control = state.control.clone();
            control.max_work = control.max_work.saturating_sub(state.work);
            control.timeout = state
                .deadline
                .map(|deadline| deadline.saturating_duration_since(Instant::now()));
            let (rows, work) =
                extensions.invoke(&name, database, &extension_args, &control, state.deadline);
            state.check(work)?;
            let rows = rows?;
            if rows.len() > state.control.max_result_rows
                || rows
                    .iter()
                    .fold(0_usize, |total, row| total.saturating_add(row.len()))
                    > state.control.max_intermediate_rows
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
        Binding::Tuple(_) => constant
            .sequence_values()
            .map(|values| vec![values])
            .ok_or_else(|| {
                SemanticError::incorrect(
                    "query/function-binding",
                    "tuple binding requires one sequential value",
                )
            }),
        Binding::Relation(_) => constant
            .sequence_values()
            .ok_or_else(|| {
                SemanticError::incorrect(
                    "query/function-binding",
                    "relation binding requires a collection of tuple values",
                )
            })?
            .iter()
            .map(|row| {
                row.sequence_values().ok_or_else(|| {
                    SemanticError::incorrect(
                        "query/function-binding",
                        "relation binding requires sequential rows",
                    )
                })
            })
            .collect(),
    }
}

fn require_stored<'a>(
    value: &'a BoundValue,
    code: &'static str,
) -> Result<&'a Value, SemanticError> {
    value.stored().ok_or_else(|| {
        SemanticError::incorrect(code, "this query function argument must be a stored scalar value, not nil or a query container")
    })
}

fn numeric_function(
    function: Function,
    args: &[BoundValue],
    state: &mut State<'_>,
) -> Result<Value, SemanticError> {
    if args.len() != 2 {
        return Err(SemanticError::incorrect(
            "query/function-arity",
            "arithmetic functions require two arguments",
        ));
    }
    let left = require_stored(&args[0], "query/numeric-type")?;
    let right = require_stored(&args[1], "query/numeric-type")?;
    let max_bytes = state.control.max_numeric_bytes.min(
        state
            .max_value_bytes
            .saturating_sub(state.stats.allocated_value_bytes),
    );
    numeric::binary(
        &function,
        left,
        right,
        &mut numeric::Budget {
            max_bytes,
            charge: |work, bytes| {
                state.check(work)?;
                state.charge_value_bytes(bytes)
            },
        },
    )
}

fn bind_output(
    row: &Row,
    binding: &Binding,
    output: &[BoundValue],
) -> Result<Vec<Row>, SemanticError> {
    if let Binding::Collection(variable) = binding {
        let [value] = output else {
            return Err(SemanticError::incorrect(
                "query/function-binding",
                "collection binding requires one sequential value",
            ));
        };
        let values = value.sequence_values().ok_or_else(|| {
            SemanticError::incorrect(
                "query/function-binding",
                "collection binding requires one sequential value",
            )
        })?;
        let mut rows = Vec::new();
        for value in values {
            let mut candidate = row.clone();
            if unify_variable(&mut candidate, variable, &value) {
                rows.push(candidate);
            }
        }
        return Ok(dedupe_rows(rows));
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
    sources: &BTreeMap<&str, SourceRef<'_>>,
    pull_budget: &mut QueryPullBudget,
    max_numeric_bytes: usize,
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
        pull_budget.check(1)?;
        let projected = basis_variables
            .iter()
            .map(|variable| {
                let value = row.get(variable).ok_or_else(|| {
                    SemanticError::incorrect(
                        "query/unbound-find-variable",
                        format!("find variable {} is unbound", variable.name()),
                    )
                })?;
                pull_budget.charge_value_bytes(join::bound_bytes(value))?;
                Ok(value.clone())
            })
            .collect::<Result<Vec<_>, _>>()?;
        basis.push(projected);
    }
    stable_dedupe_by(&mut basis, Ord::cmp);
    pull_budget.check(0)?;
    let mut output = if elements
        .iter()
        .any(|element| matches!(element, FindElement::Aggregate { .. }))
    {
        aggregate_rows(
            elements,
            &basis_variables,
            &basis,
            sources,
            pull_budget,
            max_numeric_bytes,
        )?
    } else {
        basis
            .into_iter()
            .map(|row| {
                pull_budget.check(1)?;
                elements
                    .iter()
                    .map(|element| {
                        project_element(element, &basis_variables, &row, sources, pull_budget)
                    })
                    .collect::<Result<Vec<_>, _>>()
            })
            .collect::<Result<Vec<_>, _>>()?
    };
    // Pull is a post-projection of distinct entity bindings, not a new set
    // operation on maps. Different entities may intentionally pull alike.
    // Recovered query.clj apply-pf uses mapv after relational set formation.
    let has_pull = elements
        .iter()
        .any(|element| matches!(element, FindElement::Pull { .. }));
    dedupe_query_rows(&mut output, query.with.is_empty() && !has_pull);
    pull_budget.check(0)?;
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
    sources: &BTreeMap<&str, SourceRef<'_>>,
    pull_budget: &mut QueryPullBudget,
    max_numeric_bytes: usize,
) -> Result<Vec<Vec<QueryValue>>, SemanticError> {
    let group_variables: Vec<_> = elements
        .iter()
        .filter_map(|element| match element {
            FindElement::Variable(variable) | FindElement::Pull { variable, .. } => Some(variable),
            _ => None,
        })
        .collect();
    let mut groups: Vec<Vec<&Vec<BoundValue>>> = Vec::new();
    let mut group_indices = BTreeMap::new();
    for row in basis {
        pull_budget.check(1)?;
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
        let index = *group_indices.entry(key).or_insert_with(|| {
            groups.push(Vec::new());
            groups.len() - 1
        });
        groups[index].push(row);
    }
    if groups.is_empty() && group_variables.is_empty() {
        groups.push(Vec::new());
    }
    groups
        .into_iter()
        .map(|rows| {
            pull_budget.check(rows.len())?;
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
                        aggregate(
                            *function,
                            rows.iter().map(|row| &row[index]).collect(),
                            pull_budget,
                            max_numeric_bytes,
                        )
                    }
                })
                .collect()
        })
        .collect()
}

fn aggregate(
    function: Aggregate,
    mut values: Vec<&BoundValue>,
    budget: &mut QueryPullBudget,
    max_numeric_bytes: usize,
) -> Result<QueryValue, SemanticError> {
    match function {
        Aggregate::Rand(count) => {
            if values.is_empty() {
                return Ok(QueryValue::Collection(Vec::new()));
            }
            // With-replacement output can exceed its input size. Admit before
            // allocation, and remain cooperatively interruptible while building.
            budget.check(count)?;
            let mut output = Vec::new();
            for _ in 0..count {
                budget.check(0)?;
                output.push(values[rand::random_range(0..values.len())].query_value());
            }
            Ok(QueryValue::Collection(output))
        }
        Aggregate::Sample(count) => {
            values.sort_by(|left, right| left.index_cmp(right));
            values.dedup_by(|left, right| left.index_cmp(right).is_eq());
            let count = count.min(values.len());
            for index in 0..count {
                budget.check(0)?;
                let selected = rand::random_range(index..values.len());
                values.swap(index, selected);
            }
            Ok(QueryValue::Collection(
                values
                    .into_iter()
                    .take(count)
                    .map(BoundValue::query_value)
                    .collect(),
            ))
        }
        Aggregate::Count => Ok(QueryValue::Scalar(Value::Long(values.len() as i64))),
        Aggregate::CountDistinct => {
            values.sort_by(|left, right| left.index_cmp(right));
            values.dedup_by(|left, right| left.index_cmp(right).is_eq());
            Ok(QueryValue::Scalar(Value::Long(values.len() as i64)))
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
            let max_bytes = max_numeric_bytes.min(budget.remaining_value_bytes());
            let values = values
                .into_iter()
                .map(|value| require_stored(value, "query/numeric-type"))
                .collect::<Result<Vec<_>, _>>()?;
            numeric::aggregate(
                function,
                values,
                &mut numeric::Budget {
                    max_bytes,
                    charge: |work, bytes| {
                        budget.check(work)?;
                        budget.charge_value_bytes(bytes)
                    },
                },
            )
            .map(QueryValue::Scalar)
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
        Clause::Not {
            join: Some(join), ..
        }
        | Clause::Or {
            join: Some(join), ..
        } => {
            output.extend(join.iter().cloned());
        }
        Clause::Not {
            join: None,
            clauses,
        } => {
            output.extend(variables_in_clauses(clauses));
        }
        Clause::Or {
            join: None,
            branches,
        } => {
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
    sources: &BTreeMap<&str, SourceRef<'_>>,
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
        _ => {
            pull_budget.charge_value_bytes(join::bound_bytes(&row[index]))?;
            Ok(row[index].query_value())
        }
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
        Some(BoundValue::Nil | BoundValue::Query(_)) | None => Ok(None),
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
        Some(BoundValue::Query(_)) => Err(SemanticError::incorrect(
            "query/attribute-value",
            "a query container cannot identify an attribute",
        )),
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
            Some(BoundValue::Nil | BoundValue::Query(_)) => Ok(false),
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
            Some(BoundValue::Nil | BoundValue::Query(_)) => Ok(false),
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
            Some(BoundValue::Nil | BoundValue::Query(_)) => Ok(false),
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
fn dedupe_rows(mut rows: Vec<Row>) -> Vec<Row> {
    stable_dedupe_by(&mut rows, Ord::cmp);
    rows
}
fn dedupe_query_rows(rows: &mut Vec<Vec<QueryValue>>, dedupe: bool) {
    if dedupe {
        stable_dedupe_by(rows, |left, right| {
            left.iter()
                .zip(right)
                .map(|(left, right)| left.canonical_cmp(right))
                .find(|order| !order.is_eq())
                .unwrap_or_else(|| left.len().cmp(&right.len()))
        });
    }
}

/// Set semantics with stable first-representation retention. Sorting offsets
/// avoids payload clones and the former quadratic repeated `Vec::contains`.
fn stable_dedupe_by<T>(values: &mut Vec<T>, compare: impl Fn(&T, &T) -> std::cmp::Ordering) {
    if values.len() < 2 {
        return;
    }
    let mut order: Vec<_> = (0..values.len()).collect();
    order.sort_unstable_by(|&left, &right| {
        compare(&values[left], &values[right]).then_with(|| left.cmp(&right))
    });
    let mut keep = vec![false; values.len()];
    let mut previous = None;
    for index in order {
        if previous.is_none_or(|previous| !compare(&values[previous], &values[index]).is_eq()) {
            keep[index] = true;
            previous = Some(index);
        }
    }
    let mut index = 0;
    values.retain(|_| {
        let retained = keep[index];
        index += 1;
        retained
    });
}
fn source_database<'a>(
    state: &'a State<'_>,
    source: &str,
) -> Result<&'a DatabaseValue, SemanticError> {
    find_source(&state.sources, source)
}

fn find_source<'a>(
    sources: &'a BTreeMap<&str, SourceRef<'_>>,
    source: &str,
) -> Result<&'a DatabaseValue, SemanticError> {
    match sources.get(source) {
        Some(SourceRef::Database(database)) => Ok(database),
        Some(_) => Err(SemanticError::incorrect(
            "query/source-kind",
            format!("source {source} must be a database"),
        )),
        None => Err(SemanticError::incorrect(
            "query/unknown-source",
            format!("unknown source {source}"),
        )),
    }
}
fn effective_source<'a>(source: &'a str, inherited_source: Option<&'a str>) -> &'a str {
    if source == "$" {
        inherited_source.unwrap_or(source)
    } else {
        source
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

pub(crate) fn query_value_allocation_bytes(value: &QueryValue) -> usize {
    join::query_value_bytes(value)
}
fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

impl State<'_> {
    fn check(&mut self, amount: usize) -> Result<(), SemanticError> {
        self.work = self.work.saturating_add(amount);
        if self.control.cancel.load(AtomicOrdering::Relaxed)
            || self
                .borrowed_cancel
                .is_some_and(|cancel| cancel.load(AtomicOrdering::Relaxed))
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
        if self.work > self.control.max_work {
            return Err(resource(
                "query/work-limit",
                "query exceeded its work limit",
            ));
        }
        Ok(())
    }

    fn check_row_count(&self, count: usize) -> Result<(), SemanticError> {
        if count > self.control.max_intermediate_rows {
            return Err(resource(
                "query/intermediate-limit",
                "query relation exceeded its row limit",
            ));
        }
        Ok(())
    }

    fn charge_value_bytes(&mut self, bytes: usize) -> Result<(), SemanticError> {
        self.stats.allocated_value_bytes = self.stats.allocated_value_bytes.saturating_add(bytes);
        if self.stats.allocated_value_bytes > self.max_value_bytes {
            return Err(resource(
                "query/value-byte-limit",
                "query exceeded its shared value allocation allowance",
            ));
        }
        Ok(())
    }

    fn push_row(&mut self, rows: &mut Vec<Row>, row: Row) -> Result<(), SemanticError> {
        if rows.len() >= self.control.max_intermediate_rows {
            *rows = dedupe_rows(std::mem::take(rows));
            if rows.iter().any(|existing| existing == &row) {
                return Ok(());
            }
        }
        self.check_row_count(rows.len().saturating_add(1))?;
        self.charge_value_bytes(join::row_bytes(&row))?;
        rows.push(row);
        Ok(())
    }
}
