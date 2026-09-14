//! Keyed, mutable least-recently-used ordering; callers own cache policy.
//!
//! The bundled `clojure.core.cache/LRUCache` composes value lookup with the
//! persistent `clojure.data.priority-map` to update an item's recency and remove
//! the least recent item. Atomic's caches already mutate under their owners'
//! locks; database values retain immutable payloads, not old cache versions.
//! A hash index plus linked slots preserves keyed recency without copying trees,
//! scanning resident keys, accumulating stale queue entries, or advancing a clock.
//!
//! Lookup, touch and eviction have expected O(1) work; inserting a new key is
//! amortized expected O(1). Each live key appears in the index and its slot.
//! Eviction makes the slot reusable, so slot count follows peak simultaneous
//! occupancy. Buffer capacity is retained after eviction; this is not an RSS or
//! byte-limit claim. Capacity, weights, counters and synchronization belong to
//! callers. Values need not be cloneable; callers may retain shared `Arc`s.

use std::collections::HashMap;
use std::hash::Hash;

#[derive(Debug)]
struct Entry<K, V> {
    key: K,
    value: V,
    previous: Option<usize>,
    next: Option<usize>,
}

#[derive(Debug)]
pub(crate) struct LruMap<K, V> {
    positions: HashMap<K, usize>,
    slots: Vec<Option<Entry<K, V>>>,
    free: Vec<usize>,
    least_recent: Option<usize>,
    most_recent: Option<usize>,
}

impl<K, V> Default for LruMap<K, V> {
    fn default() -> Self {
        Self {
            positions: HashMap::new(),
            slots: Vec::new(),
            free: Vec::new(),
            least_recent: None,
            most_recent: None,
        }
    }
}

impl<K: Eq + Hash + Clone, V> LruMap<K, V> {
    pub(crate) fn len(&self) -> usize {
        self.positions.len()
    }

    /// A successful lookup moves the existing entry to most recent.
    pub(crate) fn get(&mut self, key: &K) -> Option<&V> {
        let slot = *self.positions.get(key)?;
        self.touch(slot);
        Some(&self.entry(slot).value)
    }

    /// Inspect a value without changing recency.
    #[cfg(test)]
    pub(crate) fn peek(&self, key: &K) -> Option<&V> {
        self.positions.get(key).map(|&slot| &self.entry(slot).value)
    }

    /// Insert or replace one value, making its key most recent in either case.
    pub(crate) fn insert(&mut self, key: K, value: V) -> Option<V> {
        if let Some(&slot) = self.positions.get(&key) {
            let previous = std::mem::replace(&mut self.entry_mut(slot).value, value);
            self.touch(slot);
            return Some(previous);
        }
        let entry = Entry {
            key: key.clone(),
            value,
            previous: None,
            next: None,
        };
        let slot = if let Some(slot) = self.free.pop() {
            self.slots[slot] = Some(entry);
            slot
        } else {
            self.slots.push(Some(entry));
            self.slots.len() - 1
        };
        self.positions.insert(key, slot);
        self.link_most_recent(slot);
        None
    }

    /// Remove and return the least recent entry, preserving value ownership.
    pub(crate) fn pop_lru(&mut self) -> Option<(K, V)> {
        let slot = self.least_recent?;
        self.unlink(slot);
        let entry = self.slots[slot].take().expect("occupied LRU slot");
        let removed = self.positions.remove(&entry.key);
        debug_assert_eq!(removed, Some(slot));
        self.free.push(slot);
        Some((entry.key, entry.value))
    }

    /// Arbitrary map order; iteration does not update recency.
    #[cfg(test)]
    pub(crate) fn keys(&self) -> impl Iterator<Item = &K> {
        self.positions.keys()
    }

    /// Arbitrary map order; iteration does not update recency.
    #[cfg(test)]
    pub(crate) fn values(&self) -> impl Iterator<Item = &V> {
        self.positions.values().map(|&slot| &self.entry(slot).value)
    }

    fn entry(&self, slot: usize) -> &Entry<K, V> {
        self.slots[slot].as_ref().expect("occupied LRU slot")
    }

    fn entry_mut(&mut self, slot: usize) -> &mut Entry<K, V> {
        self.slots[slot].as_mut().expect("occupied LRU slot")
    }

    fn touch(&mut self, slot: usize) {
        if self.most_recent != Some(slot) {
            self.unlink(slot);
            self.link_most_recent(slot);
        }
    }

    fn unlink(&mut self, slot: usize) {
        let entry = self.entry(slot);
        let (previous, next) = (entry.previous, entry.next);
        match previous {
            Some(previous) => self.entry_mut(previous).next = next,
            None => self.least_recent = next,
        }
        match next {
            Some(next) => self.entry_mut(next).previous = previous,
            None => self.most_recent = previous,
        }
        let entry = self.entry_mut(slot);
        entry.previous = None;
        entry.next = None;
    }

    fn link_most_recent(&mut self, slot: usize) {
        let previous = self.most_recent;
        self.entry_mut(slot).previous = previous;
        match previous {
            Some(previous) => self.entry_mut(previous).next = Some(slot),
            None => self.least_recent = Some(slot),
        }
        self.most_recent = Some(slot);
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::collections::{BTreeMap, BTreeSet, VecDeque};
    use std::sync::Arc;

    fn assert_state(
        actual: &LruMap<u64, u64>,
        expected: &BTreeMap<u64, u64>,
        order: &VecDeque<u64>,
        peak: usize,
    ) {
        assert_eq!(actual.len(), expected.len());
        assert_eq!(actual.slots.len(), peak);
        assert_eq!(actual.free.len() + actual.len(), actual.slots.len());
        assert_eq!(
            actual.keys().copied().collect::<BTreeSet<_>>(),
            expected.keys().copied().collect()
        );
        let mut values: Vec<_> = actual.values().copied().collect();
        values.sort_unstable();
        let mut expected_values: Vec<_> = expected.values().copied().collect();
        expected_values.sort_unstable();
        assert_eq!(values, expected_values);
        for (key, value) in expected {
            assert_eq!(actual.peek(key), Some(value));
        }

        let mut live = BTreeSet::new();
        let mut visited_keys = VecDeque::new();
        let mut next = actual.least_recent;
        let mut previous = None;
        while let Some(slot) = next {
            assert!(live.insert(slot), "recency must not contain cycles");
            let entry = actual.entry(slot);
            assert_eq!(entry.previous, previous);
            assert_eq!(actual.positions.get(&entry.key), Some(&slot));
            visited_keys.push_back(entry.key);
            previous = Some(slot);
            next = entry.next;
        }
        assert_eq!(&visited_keys, order);
        assert_eq!(previous, actual.most_recent);
        assert_eq!(live.len(), actual.len());

        let free: BTreeSet<_> = actual.free.iter().copied().collect();
        assert_eq!(free.len(), actual.free.len(), "no duplicate free slots");
        assert!(free.is_disjoint(&live));
        for slot in &free {
            assert!(actual.slots[*slot].is_none());
        }
        assert_eq!(live.len() + free.len(), actual.slots.len());
    }

    #[test]
    fn randomized_operations_match_keyed_recency_oracle() {
        for mut random in [0x84a7_104c_9021_2e55_u64, 0x6729_a14b_e31a_9043] {
            let mut actual = LruMap::default();
            let mut expected = BTreeMap::new();
            let mut order = VecDeque::new();
            let mut peak = 0;
            for step in 0..10_000 {
                random ^= random << 13;
                random ^= random >> 7;
                random ^= random << 17;
                let key = (random >> 8) % 97;
                match random % 5 {
                    0 => assert_eq!(actual.peek(&key), expected.get(&key)),
                    1 | 2 => {
                        assert_eq!(actual.insert(key, step), expected.insert(key, step));
                        order.retain(|old| old != &key);
                        order.push_back(key);
                    }
                    3 => {
                        assert_eq!(actual.get(&key), expected.get(&key));
                        if expected.contains_key(&key) {
                            order.retain(|old| old != &key);
                            order.push_back(key);
                        }
                    }
                    _ => {
                        let removed = order
                            .pop_front()
                            .map(|key| (key, expected.remove(&key).unwrap()));
                        assert_eq!(actual.pop_lru(), removed);
                    }
                }
                peak = peak.max(expected.len());
                assert_state(&actual, &expected, &order, peak);
            }
            while let Some(key) = order.pop_front() {
                assert_eq!(
                    actual.pop_lru(),
                    Some((key, expected.remove(&key).unwrap()))
                );
                assert_state(&actual, &expected, &order, peak);
            }
            assert_eq!(actual.pop_lru(), None);
        }
    }

    #[test]
    fn replacement_touch_and_eviction_preserve_shared_payloads() {
        let mut map = LruMap::default();
        let payload = Arc::new(vec![1, 2, 3]);
        let retained = Arc::downgrade(&payload);
        assert!(map.insert("a".to_owned(), payload.clone()).is_none());
        assert!(map.insert("b".to_owned(), Arc::new(vec![4])).is_none());
        assert!(Arc::ptr_eq(map.get(&"a".to_owned()).unwrap(), &payload));
        assert!(map.get(&"missing".to_owned()).is_none());
        assert_eq!(map.peek(&"b".to_owned()).unwrap().as_slice(), &[4]);
        assert_eq!(map.pop_lru().unwrap().0, "b");

        let old = map.insert("a".to_owned(), Arc::new(vec![5])).unwrap();
        assert!(Arc::ptr_eq(&old, &payload));
        assert_eq!(map.len(), 1);
        assert_eq!(map.pop_lru().unwrap().1.as_slice(), &[5]);
        assert_eq!(map.pop_lru(), None);
        assert_eq!(map.least_recent, None);
        assert_eq!(map.most_recent, None);
        drop(old);
        assert!(retained.upgrade().is_some());
        drop(payload);
        assert!(retained.upgrade().is_none());
    }

    #[test]
    fn churn_and_complete_drains_reuse_slots() {
        let mut map = LruMap::default();
        for key in 0..64 {
            map.insert(key, key);
        }
        let allocated = map.slots.capacity();
        for key in 64..4096 {
            assert_eq!(map.pop_lru(), Some((key - 64, key - 64)));
            map.insert(key, key);
            assert_eq!(map.slots.len(), 64);
            assert_eq!(map.slots.capacity(), allocated);
        }
        while map.pop_lru().is_some() {}
        assert_eq!(map.free.len(), 64);
        for key in 0..64 {
            map.insert(key, key);
        }
        assert_eq!(map.slots.len(), 64);
        assert_eq!(map.slots.capacity(), allocated);
        for key in 0..64 {
            assert_eq!(map.pop_lru(), Some((key, key)));
        }
        assert_eq!(map.len(), 0);
        assert_eq!(map.pop_lru(), None);
    }
}
