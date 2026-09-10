//! Bounded reuse of schema-independent query preparation. Bindings, databases,
//! callback registries and results never enter this cache.
use super::*;
use std::collections::VecDeque;
use std::fmt::Write;
use std::sync::{Mutex, OnceLock};

#[derive(Clone, Debug)]
pub struct PreparedQuery {
    query: Arc<Query>,
}

impl PreparedQuery {
    pub fn new(query: &Query) -> Result<Self, SemanticError> {
        validate_query(query, query.inputs.len())?;
        Ok(Self {
            query: Arc::new(query.clone()),
        })
    }

    pub fn query(&self) -> &Query {
        &self.query
    }

    pub fn execute(
        &self,
        sources: &[QueryDataSource],
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        QueryEngine::execute_prepared(self, sources, inputs, control, None)
    }

    pub fn execute_with_extensions(
        &self,
        sources: &[QueryDataSource],
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        QueryEngine::execute_prepared(self, sources, inputs, control, Some(extensions))
    }
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct PreparedQueryCacheStats {
    pub hits: u64,
    pub misses: u64,
    pub evictions: u64,
    pub entries: usize,
    /// Conservative retention weight, including AST and structural-key bytes;
    /// this is a cache policy unit, not measured allocator usage or RSS.
    pub retained_weight: usize,
}

struct Entry {
    key: String,
    query: PreparedQuery,
    weight: usize,
}

pub struct PreparedQueryCache {
    entries: VecDeque<Entry>,
    max_entries: usize,
    max_weight: usize,
    stats: PreparedQueryCacheStats,
}

impl PreparedQueryCache {
    pub fn new(max_entries: usize, max_weight: usize) -> Self {
        Self {
            entries: VecDeque::new(),
            max_entries,
            max_weight,
            stats: PreparedQueryCacheStats::default(),
        }
    }
    pub fn stats(&self) -> PreparedQueryCacheStats {
        self.stats
    }

    pub fn prepare(&mut self, query: &Query) -> Result<PreparedQuery, SemanticError> {
        let key = structural_key(query, self.max_weight / 8);
        self.prepare_key(query, key)
    }

    fn prepare_key(
        &mut self,
        query: &Query,
        key: Option<String>,
    ) -> Result<PreparedQuery, SemanticError> {
        if let Some(key) = &key {
            if let Some(index) = self.entries.iter().position(|entry| &entry.key == key) {
                let entry = self.entries.remove(index).expect("located entry");
                let prepared = entry.query.clone();
                self.entries.push_back(entry);
                self.stats.hits = self.stats.hits.saturating_add(1);
                return Ok(prepared);
            }
        }
        self.stats.misses = self.stats.misses.saturating_add(1);
        let prepared = PreparedQuery::new(query)?;
        if let Some(key) = key {
            let weight = key
                .len()
                .saturating_mul(8)
                .saturating_add(std::mem::size_of::<Entry>());
            if self.max_entries > 0 && weight <= self.max_weight {
                while self.entries.len() >= self.max_entries
                    || self.stats.retained_weight.saturating_add(weight) > self.max_weight
                {
                    let removed = self.entries.pop_front().expect("nonempty bounded cache");
                    self.stats.retained_weight -= removed.weight;
                    self.stats.evictions += 1;
                }
                self.entries.push_back(Entry {
                    key,
                    query: prepared.clone(),
                    weight,
                });
                self.stats.retained_weight += weight;
                self.stats.entries = self.entries.len();
            }
        }
        Ok(prepared)
    }
}

// A process-local structural representation, deliberately not a persisted wire
// encoding. Unlike Query::eq this distinguishes numeric representations and
// decimal scales, which may be returned verbatim by ground/default expressions.
fn structural_key(query: &Query, max: usize) -> Option<String> {
    let float_bits = cache_material(query)?;
    struct Bounded {
        text: String,
        max: usize,
    }
    impl Write for Bounded {
        fn write_str(&mut self, value: &str) -> fmt::Result {
            if self.text.len().saturating_add(value.len()) > self.max {
                return Err(fmt::Error);
            }
            self.text.push_str(value);
            Ok(())
        }
    }
    let mut key = Bounded {
        text: String::new(),
        max,
    };
    write!(&mut key, "{query:?}").ok()?;
    // Float Debug intentionally elides NaN payload/sign bits. Ground/default
    // expressions can retain those bits, so do not reuse a different constant.
    write!(&mut key, "/float-bits:{float_bits:?}").ok()?;
    Some(key.text)
}

// This is admission to an optional cache, not a query semantic limit. Avoid
// recursive Debug/Clone on deep ASTs and never retain/collide opaque callbacks.
fn cache_material(query: &Query) -> Option<Vec<u64>> {
    enum Node<'a> {
        Query(&'a Query),
        Clause(&'a Clause),
        Value(&'a Value),
        Pull(&'a PullPattern),
        Output(&'a QueryValue),
    }
    let mut pending = vec![(Node::Query(query), 0usize)];
    let mut count = 0usize;
    let mut floats = Vec::new();
    while let Some((node, depth)) = pending.pop() {
        count += 1;
        if depth > 48 || count > 8192 || pending.len() > 8192 {
            return None;
        }
        let mut push = |node| pending.push((node, depth + 1));
        fn term_value(term: &Term) -> Option<&Value> {
            match term {
                Term::Constant(value) => Some(value),
                _ => None,
            }
        }
        match node {
            Node::Query(query) => {
                if query
                    .clauses
                    .len()
                    .saturating_add(
                        query
                            .rules
                            .iter()
                            .map(|rule| rule.clauses.len())
                            .sum::<usize>(),
                    )
                    .saturating_add(find_elements(&query.find).len())
                    > 8192
                {
                    return None;
                }
                for clause in &query.clauses {
                    push(Node::Clause(clause));
                }
                for rule in &query.rules {
                    for clause in &rule.clauses {
                        push(Node::Clause(clause));
                    }
                }
                for element in find_elements(&query.find) {
                    if let FindElement::Pull { pattern, .. } = element {
                        push(Node::Pull(pattern));
                    }
                }
            }
            Node::Clause(clause) => match clause {
                Clause::Pattern(pattern) => {
                    for term in [&pattern.entity, &pattern.attribute, &pattern.value]
                        .into_iter()
                        .chain(pattern.transaction.iter())
                        .chain(pattern.added.iter())
                    {
                        if let Some(value) = term_value(term) {
                            push(Node::Value(value));
                        }
                    }
                }
                Clause::Predicate { args, .. }
                | Clause::Rule { args, .. }
                | Clause::Function { args, .. } => {
                    if args.len() > 8192 {
                        return None;
                    }
                    for term in args {
                        if let Some(value) = term_value(term) {
                            push(Node::Value(value));
                        }
                    }
                    if let Clause::Function {
                        function: Function::Query(query),
                        ..
                    } = clause
                    {
                        push(Node::Query(query));
                    }
                }
                Clause::Not { clauses, .. } => {
                    if clauses.len() > 8192 {
                        return None;
                    }
                    for clause in clauses {
                        push(Node::Clause(clause));
                    }
                }
                Clause::Or { branches, .. } => {
                    if branches.len() > 8192 || branches.iter().map(Vec::len).sum::<usize>() > 8192
                    {
                        return None;
                    }
                    for branch in branches {
                        for clause in branch {
                            push(Node::Clause(clause));
                        }
                    }
                }
            },
            Node::Value(value) => match value {
                Value::Float(value) => floats.push(u64::from(value.to_bits())),
                Value::Double(value) => floats.push(value.to_bits()),
                Value::Tuple(values) => {
                    if values.len() > 8192 {
                        return None;
                    }
                    for value in values.iter().flatten() {
                        push(Node::Value(value));
                    }
                }
                _ => {}
            },
            Node::Pull(pattern) => {
                if pattern.attributes.len() > 8192 {
                    return None;
                }
                for attribute in &pattern.attributes {
                    if matches!(
                        attribute.transform,
                        Some(crate::PullTransform::Function { .. })
                    ) {
                        return None;
                    }
                    if let Some(value) = &attribute.default {
                        push(Node::Output(value));
                    }
                    if let Some(value) = &attribute.alias {
                        push(Node::Output(value));
                    }
                    if let Some(crate::PullNested::Pattern(pattern)) = &attribute.nested {
                        push(Node::Pull(pattern));
                    }
                }
            }
            Node::Output(value) => match value {
                QueryValue::Scalar(value) => push(Node::Value(value)),
                QueryValue::Nil => {}
                QueryValue::Tuple(values) | QueryValue::Collection(values) => {
                    if values.len() > 8192 {
                        return None;
                    }
                    for value in values {
                        push(Node::Output(value));
                    }
                }
                QueryValue::Map(values) => {
                    if values.len() > 4096 {
                        return None;
                    }
                    for (key, value) in values {
                        push(Node::Output(key));
                        push(Node::Output(value));
                    }
                }
            },
        }
    }
    Some(floats)
}

pub(super) fn cached(query: &Query) -> Result<(Option<PreparedQuery>, bool), SemanticError> {
    // Bypass borrowed execution before cloning an uncacheable query.
    let Some(key) = structural_key(query, (4 * 1024 * 1024) / 8) else {
        validate_query(query, query.inputs.len())?;
        return Ok((None, false));
    };
    static CACHE: OnceLock<Mutex<PreparedQueryCache>> = OnceLock::new();
    let mut cache = CACHE
        .get_or_init(|| Mutex::new(PreparedQueryCache::new(64, 4 * 1024 * 1024)))
        .lock()
        .unwrap_or_else(std::sync::PoisonError::into_inner);
    let before = cache.stats().hits;
    let prepared = cache.prepare_key(query, Some(key))?;
    Ok((Some(prepared), cache.stats().hits > before))
}
