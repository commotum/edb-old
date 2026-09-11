//! Bounded, discardable canonical-program decoding cache. Scope is captured
//! database lineage plus excision generation; a cache entry never grants a
//! snapshot, writer lease, or authorization to open a retired value.
use crate::program::ValidatedProgram;
use crate::{ProgramCacheStats, ProgramHash};
use std::collections::{BTreeMap, VecDeque};
use std::sync::{Arc, Mutex, MutexGuard};

pub(crate) type ProgramCacheKey = ([u8; 16], u64, ProgramHash);
pub(crate) type SharedProgramCache = Arc<Mutex<ProgramCache>>;

struct CachedProgram {
    program: Arc<ValidatedProgram>,
    weight: usize,
}

pub(crate) struct ProgramCache {
    entries: BTreeMap<ProgramCacheKey, CachedProgram>,
    lru: VecDeque<ProgramCacheKey>,
    max_entries: usize,
    max_bytes: usize,
    stats: ProgramCacheStats,
}
impl Default for ProgramCache {
    fn default() -> Self {
        Self {
            entries: BTreeMap::new(),
            lru: VecDeque::new(),
            max_entries: 64,
            max_bytes: 64 * 1024 * 1024,
            stats: ProgramCacheStats::default(),
        }
    }
}
impl ProgramCache {
    pub(crate) fn get(&mut self, key: ProgramCacheKey) -> Option<Arc<ValidatedProgram>> {
        let value = self
            .entries
            .get(&key)
            .map(|entry| Arc::clone(&entry.program));
        if value.is_some() {
            self.stats.hits = self.stats.hits.saturating_add(1);
            self.touch(key);
        } else {
            self.stats.misses = self.stats.misses.saturating_add(1);
        }
        value
    }
    pub(crate) fn insert_validated(
        &mut self,
        key: ProgramCacheKey,
        program: Arc<ValidatedProgram>,
        payload_bytes: usize,
    ) {
        self.stats.decodes = self.stats.decodes.saturating_add(1);
        self.stats.validations = self.stats.validations.saturating_add(1);
        // Canonical payload plus fixed function/key allowance is accounted
        // cache weight, not a claim about allocator RSS.
        let weight = payload_bytes.saturating_add(1024 + std::mem::size_of::<ProgramCacheKey>());
        if self.max_entries == 0 || weight > self.max_bytes {
            return;
        }
        if let Some(previous) = self.entries.remove(&key) {
            self.stats.current_bytes = self.stats.current_bytes.saturating_sub(previous.weight);
        }
        self.entries.insert(key, CachedProgram { program, weight });
        self.stats.current_bytes = self.stats.current_bytes.saturating_add(weight);
        self.touch(key);
        self.enforce_limits();
    }
    pub(crate) fn set_limits(&mut self, entries: usize, bytes: usize) {
        self.max_entries = entries;
        self.max_bytes = bytes;
        self.enforce_limits();
    }
    fn touch(&mut self, key: ProgramCacheKey) {
        self.lru.retain(|old| old != &key);
        self.lru.push_back(key);
    }
    fn enforce_limits(&mut self) {
        while self.entries.len() > self.max_entries || self.stats.current_bytes > self.max_bytes {
            let Some(key) = self.lru.pop_front() else {
                break;
            };
            if let Some(entry) = self.entries.remove(&key) {
                self.stats.current_bytes = self.stats.current_bytes.saturating_sub(entry.weight);
                self.stats.evictions = self.stats.evictions.saturating_add(1);
            }
        }
        self.stats.current_entries = self.entries.len();
    }
    pub(crate) fn stats(&self) -> ProgramCacheStats {
        self.stats
    }
}

pub(crate) fn lock(cache: &SharedProgramCache) -> MutexGuard<'_, ProgramCache> {
    cache
        .lock()
        .unwrap_or_else(std::sync::PoisonError::into_inner)
}
pub(crate) fn stats(cache: &SharedProgramCache) -> ProgramCacheStats {
    lock(cache).stats()
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn generations_and_lineages_do_not_share_decoded_admission() {
        let mut cache = ProgramCache::default();
        let program = crate::Program {
            kind: crate::ProgramKind::Transaction,
            arity: 0,
            instructions: vec![crate::Instruction::Return],
        };
        let hash = crate::program_hash(&program).unwrap();
        let value = Arc::new(ValidatedProgram::from_canonical(program));
        cache.insert_validated(([1; 16], 0, hash), Arc::clone(&value), 64);
        assert!(Arc::ptr_eq(&cache.get(([1; 16], 0, hash)).unwrap(), &value));
        assert!(cache.get(([1; 16], 1, hash)).is_none());
        assert!(cache.get(([2; 16], 0, hash)).is_none());
        cache.set_limits(1, 1);
        assert_eq!(cache.stats().current_entries, 0);
        cache.insert_validated(([1; 16], 0, hash), value, 64);
        assert_eq!(cache.stats().current_entries, 0);
        assert_eq!(cache.stats().decodes, 2);
    }
}
