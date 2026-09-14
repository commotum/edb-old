//! Bounded, discardable canonical-program decoding cache. Scope is captured
//! database lineage plus excision generation; a cache entry never grants a
//! snapshot, writer lease, or authorization to open a retired value.
use crate::ProgramHash;
use crate::collections::LruMap;
use crate::program::ValidatedProgram;
use std::sync::{Arc, Mutex, MutexGuard};

/// Cumulative canonical-program decoding work and bounded accounted cache
/// footprint. Hits cross no new decode or validation boundary.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct ProgramCacheStats {
    pub hits: u64,
    pub misses: u64,
    pub decodes: u64,
    pub validations: u64,
    pub evictions: u64,
    pub current_entries: usize,
    pub current_bytes: usize,
}

pub(crate) type ProgramCacheKey = ([u8; 16], u64, ProgramHash);
pub(crate) type SharedProgramCache = Arc<Mutex<ProgramCache>>;

struct CachedProgram {
    program: Arc<ValidatedProgram>,
    weight: usize,
}

pub(crate) struct ProgramCache {
    entries: LruMap<ProgramCacheKey, CachedProgram>,
    max_entries: usize,
    max_bytes: usize,
    stats: ProgramCacheStats,
}
impl Default for ProgramCache {
    fn default() -> Self {
        Self {
            entries: LruMap::default(),
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
        if let Some(previous) = self.entries.insert(key, CachedProgram { program, weight }) {
            self.stats.current_bytes = self.stats.current_bytes.saturating_sub(previous.weight);
        }
        self.stats.current_bytes = self.stats.current_bytes.saturating_add(weight);
        self.enforce_limits();
    }
    pub(crate) fn set_limits(&mut self, entries: usize, bytes: usize) {
        self.max_entries = entries;
        self.max_bytes = bytes;
        self.enforce_limits();
    }
    fn enforce_limits(&mut self) {
        while self.entries.len() > self.max_entries || self.stats.current_bytes > self.max_bytes {
            let Some((_, entry)) = self.entries.pop_lru() else {
                break;
            };
            self.stats.current_bytes = self.stats.current_bytes.saturating_sub(entry.weight);
            self.stats.evictions = self.stats.evictions.saturating_add(1);
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

    #[test]
    fn recency_replacement_and_limit_changes_preserve_accounting() {
        let mut cache = ProgramCache::default();
        cache.set_limits(2, usize::MAX);
        let program = crate::Program {
            kind: crate::ProgramKind::Transaction,
            arity: 0,
            instructions: vec![crate::Instruction::Return],
        };
        let hash = crate::program_hash(&program).unwrap();
        let value = Arc::new(ValidatedProgram::from_canonical(program));
        let a = ([1; 16], 0, hash);
        let b = ([2; 16], 0, hash);
        let c = ([3; 16], 0, hash);
        let overhead = 1024 + std::mem::size_of::<ProgramCacheKey>();
        cache.insert_validated(a, Arc::clone(&value), 100);
        cache.insert_validated(b, Arc::clone(&value), 200);
        assert!(Arc::ptr_eq(&cache.get(a).unwrap(), &value));
        cache.insert_validated(c, Arc::clone(&value), 300);
        assert!(cache.get(b).is_none());
        assert_eq!(cache.stats().current_bytes, overhead * 2 + 400);

        cache.insert_validated(a, Arc::clone(&value), 400);
        assert_eq!(cache.stats().current_entries, 2);
        assert_eq!(cache.stats().current_bytes, overhead * 2 + 700);
        cache.set_limits(2, overhead + 400);
        assert!(cache.get(c).is_none());
        assert!(Arc::ptr_eq(&cache.get(a).unwrap(), &value));
        assert_eq!(cache.stats().current_bytes, overhead + 400);
        assert_eq!(cache.stats().evictions, 2);

        cache.insert_validated(a, Arc::clone(&value), 401);
        assert_eq!(cache.stats().current_bytes, overhead + 400);
        assert_eq!(cache.stats().current_entries, 1);
        assert_eq!(cache.stats().decodes, 5);
        assert_eq!(cache.stats().validations, 5);
        assert_eq!(cache.stats().hits, 2);
        assert_eq!(cache.stats().misses, 2);
        cache.set_limits(0, usize::MAX);
        assert_eq!(cache.stats().current_entries, 0);
        assert_eq!(cache.stats().current_bytes, 0);
        assert_eq!(cache.stats().evictions, 3);
    }
}
