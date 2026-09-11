//! Bounded reuse of schema-independent query preparation. Bindings, databases,
//! callback registries and results never enter this cache.
use super::*;
use std::collections::VecDeque;
use std::sync::{Mutex, OnceLock};

#[derive(Clone, Debug)]
pub struct PreparedQuery {
    query: Arc<Query>,
    // Source-independent rule analysis is shared with cloned prepared values.
    // Populate only after successful validation under an invocation's controls.
    negation_validated: Arc<OnceLock<()>>,
}

impl PreparedQuery {
    pub fn new(query: &Query) -> Result<Self, SemanticError> {
        validate_query(query, query.inputs.len())?;
        Ok(Self {
            query: Arc::new(query.clone()),
            negation_validated: Arc::new(OnceLock::new()),
        })
    }

    pub fn query(&self) -> &Query {
        &self.query
    }

    pub(super) fn validate_negation(&self, state: &mut State<'_>) -> Result<(), SemanticError> {
        state.check(0)?;
        if self.negation_validated.get().is_none() {
            dependencies::validate_negation(self.query(), state)?;
            let _ = self.negation_validated.set(());
        }
        Ok(())
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
    key: Vec<u8>,
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
        key: Option<Vec<u8>>,
    ) -> Result<PreparedQuery, SemanticError> {
        if let Some(key) = &key
            && let Some(index) = self.entries.iter().position(|entry| &entry.key == key)
        {
            let entry = self.entries.remove(index).expect("located entry");
            let prepared = entry.query.clone();
            self.entries.push_back(entry);
            self.stats.hits = self.stats.hits.saturating_add(1);
            return Ok(prepared);
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

// Reuse the bounded AST encoder without storing callback objects or sources.
fn structural_key(query: &Query, max: usize) -> Option<Vec<u8>> {
    crate::encoding::query_cache_key(query, max)
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
